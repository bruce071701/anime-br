package com.animebr.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animes")
data class Anime(
    @PrimaryKey val id: Int,
    val name: String?,
    val nameAlternative: String?,
    val slug: String?,
    val imagen: String?,
    val overview: String?,
    val aired: String?,
    val type: String?,
    @ColumnInfo(defaultValue = "0") val status: Int?,
    val genres: String?,
    val rating: String?,
    val trailer: String?,
    val voteAverage: String?,
    @ColumnInfo(defaultValue = "0") val visitas: Long?,
    @ColumnInfo(defaultValue = "0") val isDubbing: Int?,
    @ColumnInfo(defaultValue = "0") val nums: Int?,
    @ColumnInfo(defaultValue = "0") val isTopic: Int?,
    val createdAt: String?
)
