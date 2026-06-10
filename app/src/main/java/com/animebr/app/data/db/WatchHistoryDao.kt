package com.animebr.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.animebr.app.data.model.WatchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistory>>

    @Query("SELECT * FROM watch_history WHERE animeId = :animeId AND episodeId = :episodeId LIMIT 1")
    suspend fun getHistory(animeId: Int, episodeId: Int): WatchHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: WatchHistory)

    @Query("DELETE FROM watch_history WHERE animeId = :animeId AND episodeId = :episodeId")
    suspend fun delete(animeId: Int, episodeId: Int)

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 20): Flow<List<WatchHistory>>

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
