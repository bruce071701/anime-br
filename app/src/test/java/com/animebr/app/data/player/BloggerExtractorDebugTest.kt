package com.animebr.app.data.player

import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Test
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Debug test to trace each step of the blogger video extraction.
 */
class BloggerExtractorDebugTest {

    private val testVideoGUrl =
        "https://www.blogger.com/video.g?token=AD6v5dyB-fdSYgPnTfqbLm70ELRS-K0Qk-YKGt-1FSGAnJWyVN5lQHEhh1sdyb44ESfpbjC_1uJSZVJ9zmh7ll_HqabBkiYq-SHYHUYjkCB01-7_5klrwjNI7Xpm_c-XCdoq0LeokHc"

    private val userAgent =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"

    @Test
    fun `debug full extraction flow`() = runBlocking {
        val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
        val client = OkHttpClient.Builder()
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

        // Step 1: Fetch video.g page
        println("=== Step 1: Fetch video.g page ===")
        val pageRequest = Request.Builder()
            .url(testVideoGUrl)
            .header("User-Agent", userAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        val pageResponse = client.newCall(pageRequest).execute()
        println("Status: ${pageResponse.code}")
        val html = pageResponse.body?.string() ?: ""
        println("HTML length: ${html.length}")

        // Step 2: Extract f.sid and bl
        println("\n=== Step 2: Extract RPC metadata ===")
        val fSid = extractJsonString(html, "FdrFJe")
        val bl = extractJsonString(html, "cfb2h")
        println("f.sid: $fSid")
        println("bl: $bl")

        if (fSid == null || bl == null) {
            println("FAILED: Could not find RPC metadata")
            return@runBlocking
        }

        // Step 3: Extract token
        val token = "AD6v5dyB-fdSYgPnTfqbLm70ELRS-K0Qk-YKGt-1FSGAnJWyVN5lQHEhh1sdyb44ESfpbjC_1uJSZVJ9zmh7ll_HqabBkiYq-SHYHUYjkCB01-7_5klrwjNI7Xpm_c-XCdoq0LeokHc"

        // Step 4: Build RPC request
        println("\n=== Step 3: Build and execute RPC ===")
        val reqId = (System.currentTimeMillis() % 100000).toInt()
        val rpcUrl = buildRpcUrl(fSid, bl, reqId)
        println("RPC URL: $rpcUrl")

        val rpcBody = buildRpcBody(token)
        println("RPC Body: $rpcBody")

        val rpcRequest = Request.Builder()
            .url(rpcUrl)
            .post(rpcBody.toRequestBody("application/x-www-form-urlencoded;charset=UTF-8".toMediaType()))
            .header("User-Agent", userAgent)
            .header("Referer", testVideoGUrl)
            .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("X-Same-Domain", "1")
            .header("Origin", "https://www.blogger.com")
            .build()

        val rpcResponse = client.newCall(rpcRequest).execute()
        println("RPC Status: ${rpcResponse.code}")
        val responseText = rpcResponse.body?.string() ?: ""
        println("Response length: ${responseText.length}")
        println("Response first 500 chars: ${responseText.take(500)}")

        // Step 5: Parse URLs
        println("\n=== Step 4: Parse video URLs ===")
        val urls = parseVideoUrls(responseText)
        println("Found ${urls.size} URLs:")
        urls.forEach { println("  $it") }
    }

    private fun extractJsonString(html: String, key: String): String? {
        val pattern = Regex(""""${Regex.escape(key)}"\s*:\s*"([^"]+)"""")
        val match = pattern.find(html) ?: return null
        return match.groupValues[1]
            .replace("\\u003d", "=")
            .replace("\\u0026", "&")
            .replace("\\/", "/")
    }

    private fun buildRpcUrl(fSid: String, bl: String, reqId: Int): String {
        val params = mapOf(
            "rpcids" to "WcwnYd",
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
        val innerPayload = """["$token",null,false]"""
        val escaped = innerPayload.replace("\"", "\\\"")
        val fReq = """[[["WcwnYd","$escaped",null,"generic"]]]"""
        return "f.req=${URLEncoder.encode(fReq, "UTF-8")}&at="
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
        val pattern = Regex("""(https?://[a-zA-Z0-9\-_.]+\.googlevideo\.com/videoplayback\?[^"'\\,\]\s]+)""")
        pattern.findAll(unescaped).forEach { match ->
            val url = match.groupValues[1]
            if (seen.add(url)) urls.add(url)
        }
        return urls
    }
}
