package com.animebr.app.data.download

import android.content.Context
import com.animebr.app.data.db.DownloadDao
import com.animebr.app.data.model.Download
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val okHttpClient: OkHttpClient,
    private val notificationHelper: DownloadNotificationHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<Int, Job>()

    private val downloadDir: File
        get() {
            val dir = File(context.getExternalFilesDir(null), "downloads")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getAllDownloads(): Flow<List<Download>> = downloadDao.getAllDownloads()

    suspend fun startDownload(
        animeId: Int, episodeId: Int, animeName: String?,
        episodeTitle: String?, episodeNumber: Int, videoUrl: String
    ) {
        val existing = downloadDao.getDownloadByEpisodeId(episodeId)
        if (existing != null && existing.status == Download.STATUS_COMPLETED) return
        if (existing != null && existing.status == Download.STATUS_DOWNLOADING) return

        val download = existing?.copy(status = Download.STATUS_PENDING) ?: Download(
            animeId = animeId, episodeId = episodeId, animeName = animeName,
            episodeTitle = episodeTitle, episodeNumber = episodeNumber,
            videoUrl = videoUrl, filePath = null
        )
        val id = downloadDao.insert(download).toInt()
        executeDownload(id, videoUrl, animeName ?: "Download")
    }

    fun pauseDownload(downloadId: Int) {
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)
        scope.launch {
            downloadDao.updateStatus(downloadId, Download.STATUS_PAUSED)
            notificationHelper.cancel(downloadId)
        }
    }

    fun resumeDownload(downloadId: Int) {
        scope.launch {
            val download = downloadDao.getDownloadById(downloadId) ?: return@launch
            downloadDao.updateStatus(downloadId, Download.STATUS_PENDING)
            executeDownload(downloadId, download.videoUrl, download.animeName ?: "Download")
        }
    }

    suspend fun deleteDownload(downloadId: Int) {
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)
        val download = downloadDao.getDownloadById(downloadId)
        download?.filePath?.let { File(it).delete() }
        downloadDao.deleteById(downloadId)
        notificationHelper.cancel(downloadId)
    }

    suspend fun deleteAllDownloads() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        downloadDir.listFiles()?.forEach { it.delete() }
        downloadDao.deleteAll()
    }

    private fun executeDownload(downloadId: Int, videoUrl: String, title: String) {
        val job = scope.launch {
            try {
                downloadDao.updateStatus(downloadId, Download.STATUS_DOWNLOADING)
                notificationHelper.showProgress(downloadId, title, 0, indeterminate = true)

                if (videoUrl.contains(".m3u8")) {
                    downloadHls(downloadId, videoUrl, title)
                } else {
                    downloadDirect(downloadId, videoUrl, title)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Paused
            } catch (e: Exception) {
                downloadDao.updateStatus(downloadId, Download.STATUS_FAILED)
                notificationHelper.showFailed(downloadId, title)
            }
        }
        activeJobs[downloadId] = job
    }

    private suspend fun downloadHls(downloadId: Int, m3u8Url: String, title: String) {
        val download = downloadDao.getDownloadById(downloadId) ?: return
        val outputFile = File(downloadDir, "ep_${download.episodeId}.mp4")

        // Fetch m3u8 playlist
        val playlistResponse = okHttpClient.newCall(
            Request.Builder().url(m3u8Url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                .build()
        ).execute()

        if (!playlistResponse.isSuccessful) {
            downloadDao.updateStatus(downloadId, Download.STATUS_FAILED)
            notificationHelper.showFailed(downloadId, title)
            return
        }

        val playlistContent = playlistResponse.body?.string() ?: ""
        val baseUrl = m3u8Url.substringBeforeLast("/") + "/"

        // Parse segment URLs
        val segments = playlistContent.lines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { if (it.startsWith("http")) it else baseUrl + it }

        if (segments.isEmpty()) {
            downloadDao.updateStatus(downloadId, Download.STATUS_FAILED)
            notificationHelper.showFailed(downloadId, title)
            return
        }

        val totalSegments = segments.size
        val outputStream = FileOutputStream(outputFile)

        try {
            segments.forEachIndexed { index, segmentUrl ->
                val segResponse = okHttpClient.newCall(
                    Request.Builder().url(segmentUrl)
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                        .build()
                ).execute()

                if (segResponse.isSuccessful) {
                    segResponse.body?.byteStream()?.use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }

                val progress = ((index + 1) * 100) / totalSegments
                downloadDao.updateProgress(downloadId, (index + 1).toLong(), totalSegments.toLong())
                notificationHelper.showProgress(downloadId, title, progress)
            }

            outputStream.flush()
            val fileSize = outputFile.length()
            downloadDao.updateProgress(downloadId, fileSize, fileSize)
            downloadDao.markCompleted(downloadId, outputFile.absolutePath)
            notificationHelper.showComplete(downloadId, title)
        } finally {
            outputStream.close()
        }
    }

    private suspend fun downloadDirect(downloadId: Int, videoUrl: String, title: String) {
        val requestBuilder = Request.Builder().url(videoUrl)

        if (videoUrl.contains("googlevideo.com")) {
            requestBuilder
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36")
                .header("Referer", videoUrl)
                .header("Accept", "*/*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Sec-Fetch-Dest", "video")
                .header("Sec-Fetch-Mode", "no-cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("sec-ch-ua", "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"macOS\"")
                .header("x-browser-channel", "stable")
                .header("x-browser-copyright", "Copyright 2026 Google LLC. All Rights Reserved.")
                .header("x-browser-validation", "z5FJMLtwNd1Yt40OJgdaJ8rqye0=")
                .header("x-browser-year", "2026")
                .header("x-client-data", "CKmdygEIlKHLAQiGoM0BCMa/zwEIzMeUMAjsyZQwCP3KlDAIrcuUMAjxy5QwCM3MlDAI0MyUMAjgzJQwCOLMlDAI7cyUMAj/zJQw")
        } else {
            requestBuilder.header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
        }

        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) {
            downloadDao.updateStatus(downloadId, Download.STATUS_FAILED)
            notificationHelper.showFailed(downloadId, title)
            return
        }

        val body = response.body ?: run {
            downloadDao.updateStatus(downloadId, Download.STATUS_FAILED)
            notificationHelper.showFailed(downloadId, title)
            return
        }

        val contentLength = body.contentLength()
        val totalSize = if (contentLength > 0) contentLength else 0L
        if (totalSize > 0) downloadDao.updateProgress(downloadId, 0, totalSize)

        val download = downloadDao.getDownloadById(downloadId) ?: return
        val file = File(downloadDir, "ep_${download.episodeId}.mp4")
        val outputStream = FileOutputStream(file)
        var downloadedBytes = 0L
        val buffer = ByteArray(8192)
        val inputStream = body.byteStream()
        var lastNotifyTime = 0L

        try {
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastNotifyTime > 500) {
                    lastNotifyTime = now
                    val fileSize = if (totalSize > 0) totalSize else downloadedBytes + 1
                    downloadDao.updateProgress(downloadId, downloadedBytes, fileSize)
                    val progress = if (totalSize > 0) ((downloadedBytes * 100) / totalSize).toInt() else -1
                    if (progress >= 0) notificationHelper.showProgress(downloadId, title, progress)
                    else notificationHelper.showProgress(downloadId, title, 0, indeterminate = true)
                }
            }

            downloadDao.updateProgress(downloadId, downloadedBytes, downloadedBytes)
            outputStream.flush()
            downloadDao.markCompleted(downloadId, file.absolutePath)
            notificationHelper.showComplete(downloadId, title)
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }
}
