package com.animebr.app.data.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts signed googlevideo.com playback URLs from Blogger video.g player pages.
 * Ported from the Python blogger_video_extractor.py script.
 */
@Singleton
class BloggerVideoExtractor @Inject constructor() {

    companion object {
        private const val RPC_ID = "WcwnYd"
        private const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
        private const val PLAYBACK_USER_AGENT = BROWSER_USER_AGENT
        private const val DEFAULT_CLIENT = "WEB_EMBEDDED_PLAYER"
        private const val FALLBACK_CLIENT_VERSION = "1.20260524.00.00"
        private const val YOUTUBE_EMBED_URL = "https://www.youtube.com/embed/"
    }

    @Volatile
    private var cachedClientVersion: String? = null

    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore.getOrPut(url.host) { mutableListOf() }.addAll(cookies)
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        })
        .build()

    /**
     * Extract video direct URL from a blogger video.g URL.
     * Returns the best quality googlevideo.com URL or null.
     */
    suspend fun extractVideoUrl(videoGUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val token = parseToken(videoGUrl) ?: return@withContext null

            // Step 1: Fetch video.g page (gets cookies + RPC metadata)
            val originReferer = getOriginReferer(videoGUrl)
            val pageHtml = fetchPage(videoGUrl, originReferer) ?: return@withContext null

            // Step 2: Parse f.sid and bl
            val fSid = extractJsonString(pageHtml, "FdrFJe") ?: return@withContext null
            val bl = extractJsonString(pageHtml, "cfb2h") ?: return@withContext null

            // Step 3: Build and execute batchexecute RPC
            val rpcUrl = buildRpcUrl(fSid, bl)
            val rpcBody = buildRpcBody(token)
            val rpcResponse = executeRpc(rpcUrl, rpcBody, videoGUrl) ?: return@withContext null

            // Step 4: Parse googlevideo URLs
            val urls = parseVideoUrls(rpcResponse)
            if (urls.isEmpty()) return@withContext null

            // Return highest quality (prefer itag=22 which is 720p)
            val bestUrl = urls.find { it.contains("itag=22") } ?: urls.last()

            // Step 5: Add client params
            addPlaybackClientParams(bestUrl)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Build playback headers required by googlevideo.com to avoid 403.
     * Must be set on ExoPlayer's DataSource.
     */
    fun getPlaybackHeaders(videoUrl: String): Map<String, String> {
        return mapOf(
            "Accept" to "*/*",
            "Accept-Language" to "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
            "Range" to "bytes=0-",
            "Referer" to videoUrl,
            "User-Agent" to PLAYBACK_USER_AGENT,
            "Sec-Fetch-Dest" to "video",
            "Sec-Fetch-Mode" to "no-cors",
            "Sec-Fetch-Site" to "same-origin",
            "sec-ch-ua" to "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
            "sec-ch-ua-mobile" to "?0",
            "sec-ch-ua-platform" to "\"macOS\"",
            "x-browser-channel" to "stable",
            "x-browser-copyright" to "Copyright 2026 Google LLC. All Rights Reserved.",
            "x-browser-validation" to "z5FJMLtwNd1Yt40OJgdaJ8rqye0=",
            "x-browser-year" to "2026",
            "x-client-data" to "CKmdygEIlKHLAQiGoM0BCMa/zwEIzMeUMAjsyZQwCP3KlDAIrcuUMAjxy5QwCM3MlDAI0MyUMAjgzJQwCOLMlDAI7cyUMAj/zJQw"
        )
    }

    // --- Private helpers ---

    private fun parseToken(url: String): String? {
        val uri = URI(url)
        val query = uri.query ?: return null
        return query.split("&")
            .map { it.split("=", limit = 2) }
            .find { it[0] == "token" }
            ?.getOrNull(1)
    }

    private fun getOriginReferer(videoGUrl: String): String? {
        val uri = URI(videoGUrl)
        val query = uri.query ?: return null
        val origin = query.split("&")
            .map { it.split("=", limit = 2) }
            .find { it[0] == "origin" }
            ?.getOrNull(1) ?: return null
        return if (origin.startsWith("http")) origin else "https://$origin/"
    }

    private fun fetchPage(url: String, referer: String?): String? {
        val builder = Request.Builder()
            .url(url)
            .header("User-Agent", BROWSER_USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
        if (referer != null) {
            builder.header("Referer", referer)
        }
        val response = client.newCall(builder.build()).execute()
        return if (response.isSuccessful) response.body?.string() else null
    }

    private fun extractJsonString(html: String, key: String): String? {
        val pattern = Regex(""""${Regex.escape(key)}"\s*:\s*"([^"]+)"""")
        val match = pattern.find(html) ?: return null
        return match.groupValues[1]
            .replace("\\u003d", "=")
            .replace("\\u0026", "&")
            .replace("\\/", "/")
    }

    private fun buildRpcUrl(fSid: String, bl: String): String {
        val reqId = (System.currentTimeMillis() % 100000).toInt()
        val params = mapOf(
            "rpcids" to RPC_ID,
            "source-path" to "/video.g",
            "f.sid" to fSid,
            "bl" to bl,
            "hl" to "en-US",
            "_reqid" to reqId.toString(),
            "rt" to "c"
        )
        val query = params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        return "https://www.blogger.com/_/BloggerVideoPlayerUi/data/batchexecute?$query"
    }

    private fun buildRpcBody(token: String): String {
        // Matches Python: [[[RPC_ID, json.dumps([token, None, False]), None, "generic"]]]
        val innerPayload = """["$token",null,false]"""
        val escaped = innerPayload.replace("\"", "\\\"")
        val fReq = """[[["WcwnYd","$escaped",null,"generic"]]]"""
        return "f.req=${URLEncoder.encode(fReq, "UTF-8")}&at="
    }

    private fun executeRpc(url: String, body: String, referer: String): String? {
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/x-www-form-urlencoded;charset=UTF-8".toMediaType()))
            .header("User-Agent", BROWSER_USER_AGENT)
            .header("Referer", referer)
            .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            .header("Accept", "*/*")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("X-Same-Domain", "1")
            .header("Origin", "https://www.blogger.com")
            .header("Sec-Fetch-Dest", "empty")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Site", "same-origin")
            .header("sec-ch-ua", "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"macOS\"")
            .build()

        val response = client.newCall(request).execute()
        return if (response.isSuccessful) response.body?.string() else null
    }

    private fun parseVideoUrls(responseText: String): List<String> {
        val urls = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        val unescaped = responseText
            .replace("\\\\u003d", "=")
            .replace("\\\\u0026", "&")
            .replace("\\u003d", "=")
            .replace("\\u0026", "&")
            .replace("\\/", "/")
            .replace("\\\\", "\\")

        // googlevideo URLs can contain commas (e.g. rms=au,au) and == (base64 sig)
        // They are terminated by a quote " in the JSON
        val pattern = Regex("""(https?://[a-zA-Z0-9\-_.]+\.googlevideo\.com/videoplayback\?[^"\\]+)""")

        pattern.findAll(unescaped).forEach { match ->
            var url = match.groupValues[1].trimEnd(']', ' ', '\n', '\r')
            if (seen.add(url)) {
                urls.add(url)
            }
        }

        return urls
    }

    private fun addPlaybackClientParams(url: String): String {
        val cpn = generateCpn()
        val clientVersion = fetchClientVersion()
        val separator = if (url.contains("?")) "&" else "?"
        return "${url}${separator}cpn=$cpn&c=$DEFAULT_CLIENT&cver=$clientVersion"
    }

    /**
     * Dynamically fetch the latest INNERTUBE_CLIENT_VERSION from YouTube embed page.
     * Caches the result. Falls back to hardcoded version on failure.
     */
    private fun fetchClientVersion(): String {
        cachedClientVersion?.let { return it }
        return try {
            val request = Request.Builder()
                .url(YOUTUBE_EMBED_URL)
                .header("User-Agent", BROWSER_USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val pattern = Regex(""""INNERTUBE_CLIENT_VERSION"\s*:\s*"([^"]+)"""")
            val match = pattern.find(body)
            val version = match?.groupValues?.get(1) ?: FALLBACK_CLIENT_VERSION
            cachedClientVersion = version
            version
        } catch (e: Exception) {
            FALLBACK_CLIENT_VERSION
        }
    }

    private fun generateCpn(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        return (1..16).map { alphabet.random() }.joinToString("")
    }
}
