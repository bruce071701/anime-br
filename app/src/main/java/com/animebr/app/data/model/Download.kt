package com.animebr.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class Download(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animeId: Int,
    val episodeId: Int,
    val animeName: String?,
    val episodeTitle: String?,
    val episodeNumber: Int,
    val videoUrl: String,
    val filePath: String?,
    val fileSize: Long = 0,
    val downloadedSize: Long = 0,
    val status: Int = STATUS_PENDING, // 0=pending, 1=downloading, 2=paused, 3=completed, 4=failed
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_DOWNLOADING = 1
        const val STATUS_PAUSED = 2
        const val STATUS_COMPLETED = 3
        const val STATUS_FAILED = 4
    }

    val progress: Float
        get() = if (fileSize > 0) downloadedSize.toFloat() / fileSize else 0f
}
