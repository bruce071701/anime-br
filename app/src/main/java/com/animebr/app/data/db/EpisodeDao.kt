package com.animebr.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.animebr.app.data.model.Episode
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes WHERE animeId = :animeId ORDER BY CAST(nums AS INTEGER) DESC")
    fun getEpisodesByAnimeIdDesc(animeId: Int): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE animeId = :animeId ORDER BY CAST(nums AS INTEGER) ASC")
    fun getEpisodesByAnimeIdAsc(animeId: Int): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getEpisodeById(id: Int): Episode?

    @Query("SELECT * FROM episodes ORDER BY createdAt DESC LIMIT :limit")
    fun getLatestEpisodes(limit: Int = 20): Flow<List<Episode>>

    /**
     * Group by animeId, get the episode with max nums (latest episode),
     * order by createdAt desc. Returns one row per anime.
     */
    @Query("""
        SELECT e.* FROM episodes e
        INNER JOIN (
            SELECT animeId, MAX(CAST(nums AS INTEGER)) as maxNums
            FROM episodes
            GROUP BY animeId
        ) grouped ON e.animeId = grouped.animeId AND CAST(e.nums AS INTEGER) = grouped.maxNums
        GROUP BY e.animeId
        ORDER BY e.createdAt DESC
        LIMIT :limit
    """)
    fun getLatestEpisodesGrouped(limit: Int = 50): Flow<List<Episode>>

    @Query("SELECT COUNT(*) FROM episodes WHERE animeId = :animeId")
    suspend fun getEpisodeCount(animeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(episodes: List<Episode>)
}
