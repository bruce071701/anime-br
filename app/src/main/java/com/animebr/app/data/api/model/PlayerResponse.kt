package com.animebr.app.data.api.model

import com.animebr.app.data.model.Player
import com.google.gson.annotations.SerializedName

data class PlayerResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("animeId") val animeId: Int,
    @SerializedName("episodeId") val episodeId: Int,
    @SerializedName("link") val link: String?,
    @SerializedName("server") val server: String?,
    @SerializedName("embed") val embed: String?,
    @SerializedName("status") val status: Int
) {
    fun toPlayer(): Player = Player(
        id = id,
        animeId = animeId,
        episodeId = episodeId,
        link = link,
        server = server,
        embed = embed,
        status = status,
        createdAt = null
    )
}
