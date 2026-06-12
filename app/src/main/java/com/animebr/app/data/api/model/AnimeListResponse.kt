package com.animebr.app.data.api.model

import com.google.gson.annotations.SerializedName

data class AnimeListResponse(
    @SerializedName("animes") val animes: List<AnimeResponse>,
    @SerializedName("episodes") val episodes: List<EpisodeResponse>
)

data class AnimeResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("nameAlternative") val nameAlternative: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("imagen") val imagen: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("aired") val aired: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("genres") val genres: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("trailer") val trailer: String?,
    @SerializedName("voteAverage") val voteAverage: String?,
    @SerializedName("visitas") val visitas: Long?,
    @SerializedName("isDubbing") val isDubbing: Int?,
    @SerializedName("nums") val nums: Int?,
    @SerializedName("isTopic") val isTopic: Int?
)

data class EpisodeResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("animeId") val animeId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("imagen") val imagen: String?,
    @SerializedName("visitas") val visitas: Long?,
    @SerializedName("nums") val nums: String?,
    @SerializedName("aired") val aired: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("createdAt") val createdAt: String?
)
