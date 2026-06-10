package com.animebr.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animeId: Int,
    val episodeId: Int,
    val episodeNumber: Int,
    val progress: Long,
    val duration: Long,
    val lastWatchedAt: Long
)
