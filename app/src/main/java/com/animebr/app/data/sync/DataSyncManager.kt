package com.animebr.app.data.sync

import android.util.Log
import com.animebr.app.data.api.AnimeListApiService
import com.animebr.app.data.db.AnimeDao
import com.animebr.app.data.db.EpisodeDao
import com.animebr.app.data.model.Anime
import com.animebr.app.data.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles incremental data sync from the server.
 * Called silently at app startup, does not block UI.
 * Uses INSERT OR REPLACE to upsert new/updated records.
 */
@Singleton
class DataSyncManager @Inject constructor(
    private val apiService: AnimeListApiService,
    private val animeDao: AnimeDao,
    private val episodeDao: EpisodeDao
) {
    companion object {
        private const val TAG = "DataSync"
    }

    suspend fun sync() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting incremental sync...")
            val response = apiService.getAnimeList()

            // Convert and upsert animes
            val animes = response.animes.map { it.toEntity() }
            if (animes.isNotEmpty()) {
                animeDao.upsertAll(animes)
                Log.d(TAG, "Synced ${animes.size} animes")
            }

            // Convert and upsert episodes
            val episodes = response.episodes.map { it.toEntity() }
            if (episodes.isNotEmpty()) {
                episodeDao.upsertAll(episodes)
                Log.d(TAG, "Synced ${episodes.size} episodes")
            }

            Log.d(TAG, "Sync complete")
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed: ${e.message}")
            // Silent failure - app continues with local data
        }
    }

    private fun com.animebr.app.data.api.model.AnimeResponse.toEntity() = Anime(
        id = id,
        name = name,
        nameAlternative = nameAlternative,
        slug = slug,
        imagen = imagen,
        overview = overview,
        aired = aired,
        type = type,
        status = status,
        genres = genres,
        rating = rating,
        trailer = trailer,
        voteAverage = voteAverage,
        visitas = visitas,
        isDubbing = isDubbing,
        nums = nums,
        isTopic = isTopic,
        createdAt = null
    )

    private fun com.animebr.app.data.api.model.EpisodeResponse.toEntity() = Episode(
        id = id,
        animeId = animeId,
        title = title,
        imagen = imagen,
        overview = null,
        url = null,
        visitas = visitas,
        nums = nums,
        aired = aired,
        status = status,
        createdAt = createdAt
    )
}
