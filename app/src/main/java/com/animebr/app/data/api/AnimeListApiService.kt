package com.animebr.app.data.api

import com.animebr.app.data.api.model.AnimeListResponse
import retrofit2.http.GET

interface AnimeListApiService {

    @GET("digital/anime_list")
    suspend fun getAnimeList(): AnimeListResponse
}
