package com.animebr.app.data.api

import com.animebr.app.data.api.model.PlayerResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PlayerApiService {

    @GET("digital/player/{episodeId}")
    suspend fun getPlayers(@Path("episodeId") episodeId: Int): List<PlayerResponse>
}
