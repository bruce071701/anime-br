package com.animebr.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey val id: Int,
    val animeId: Int?,
    val episodeId: Int?,
    val link: String?,
    val server: String?,
    val embed: String?,
    @ColumnInfo(defaultValue = "0") val status: Int?,
    val createdAt: String?
)
