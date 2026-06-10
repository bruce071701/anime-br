package com.animebr.app.data.repository

import com.animebr.app.data.api.PlayerApiService
import com.animebr.app.data.api.model.PlayerResponse
import com.animebr.app.data.db.AnimeDao
import com.animebr.app.data.db.EpisodeDao
import com.animebr.app.data.db.FavoriteDao
import com.animebr.app.data.db.WatchHistoryDao
import com.animebr.app.data.model.Anime
import com.animebr.app.data.model.Episode
import com.animebr.app.data.model.Favorite
import com.animebr.app.data.model.Player
import com.animebr.app.data.model.WatchHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeRepository @Inject constructor(
    private val animeDao: AnimeDao,
    private val episodeDao: EpisodeDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val playerApiService: PlayerApiService
) {
    // Anime queries
    fun getAllAnimes(): Flow<List<Anime>> = animeDao.getAllAnimes()
    fun getAllAnimesAlphabetical(): Flow<List<Anime>> = animeDao.getAllAnimesAlphabetical()
    fun getRecentlyAdded(limit: Int = 20): Flow<List<Anime>> = animeDao.getRecentlyAdded(limit)
    fun getMostViewed(limit: Int = 20): Flow<List<Anime>> = animeDao.getMostViewed(limit)
    fun getMostPopular(limit: Int = 20): Flow<List<Anime>> = animeDao.getMostPopular(limit)
    fun searchAnimes(query: String): Flow<List<Anime>> = animeDao.searchAnimes(query)
    fun getAnimesByGenre(genre: String): Flow<List<Anime>> = animeDao.getAnimesByGenre(genre)
    suspend fun getAnimeById(id: Int): Anime? = animeDao.getAnimeById(id)

    // Episode queries
    fun getEpisodesByAnimeId(animeId: Int, ascending: Boolean = false): Flow<List<Episode>> =
        if (ascending) episodeDao.getEpisodesByAnimeIdAsc(animeId)
        else episodeDao.getEpisodesByAnimeIdDesc(animeId)
    fun getLatestEpisodes(limit: Int = 20): Flow<List<Episode>> = episodeDao.getLatestEpisodes(limit)
    fun getLatestEpisodesGrouped(limit: Int = 50): Flow<List<Episode>> = episodeDao.getLatestEpisodesGrouped(limit)
    suspend fun getEpisodeById(id: Int): Episode? = episodeDao.getEpisodeById(id)

    // Player - always fetch from API, no local caching
    suspend fun fetchPlayers(episodeId: Int): List<Player> {
        return try {
            val response = playerApiService.getPlayers(episodeId)
            response.map { it.toPlayer() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Watch history
    fun getWatchHistory(limit: Int = 20): Flow<List<WatchHistory>> = watchHistoryDao.getRecentHistory(limit)
    suspend fun getHistoryForEpisode(animeId: Int, episodeId: Int): WatchHistory? =
        watchHistoryDao.getHistory(animeId, episodeId)
    suspend fun saveWatchProgress(history: WatchHistory) = watchHistoryDao.insertOrUpdate(history)
    suspend fun clearAllHistory() = watchHistoryDao.clearAll()

    // Favorites
    fun getAllFavorites(): Flow<List<Favorite>> = favoriteDao.getAllFavorites()
    fun isFavorite(animeId: Int): Flow<Boolean> = favoriteDao.isFavorite(animeId)
    suspend fun addFavorite(animeId: Int) {
        favoriteDao.addFavorite(Favorite(animeId = animeId, createdAt = System.currentTimeMillis()))
    }
    suspend fun removeFavorite(animeId: Int) {
        favoriteDao.removeFavorite(animeId)
    }
}
