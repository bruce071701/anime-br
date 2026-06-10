package com.animebr.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey val id: Int,
    val animeId: Int?,
    val title: String?,
    val imagen: String?,
    val overview: String?,
    val url: String?,
    @ColumnInfo(defaultValue = "0") val visitas: Long?,
    val nums: String?,
    val aired: String?,
    @ColumnInfo(defaultValue = "0") val status: Int?,
    val createdAt: String?
)
