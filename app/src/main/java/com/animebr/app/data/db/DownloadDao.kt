package com.animebr.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.animebr.app.data.model.Download
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<Download>>

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    suspend fun getAllDownloadsSync(): List<Download>

    @Query("SELECT * FROM downloads WHERE status IN (0, 1, 2) ORDER BY createdAt DESC")
    fun getActiveDownloads(): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE status = 3 ORDER BY createdAt DESC")
    fun getCompletedDownloads(): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: Int): Download?

    @Query("SELECT * FROM downloads WHERE episodeId = :episodeId LIMIT 1")
    suspend fun getDownloadByEpisodeId(episodeId: Int): Download?

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE episodeId = :episodeId AND status != 4)")
    fun isDownloaded(episodeId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: Download): Long

    @Update
    suspend fun update(download: Download)

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Int)

    @Query("UPDATE downloads SET downloadedSize = :downloadedSize, fileSize = :fileSize WHERE id = :id")
    suspend fun updateProgress(id: Int, downloadedSize: Long, fileSize: Long)

    @Query("UPDATE downloads SET filePath = :filePath, status = 3 WHERE id = :id")
    suspend fun markCompleted(id: Int, filePath: String)

    @Delete
    suspend fun delete(download: Download)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}
