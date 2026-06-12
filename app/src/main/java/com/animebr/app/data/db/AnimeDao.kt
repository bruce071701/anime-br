package com.animebr.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.animebr.app.data.model.Anime
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM animes ORDER BY id DESC")
    fun getAllAnimes(): Flow<List<Anime>>

    @Query("SELECT * FROM animes ORDER BY name ASC")
    fun getAllAnimesAlphabetical(): Flow<List<Anime>>

    @Query("SELECT * FROM animes WHERE id = :id")
    suspend fun getAnimeById(id: Int): Anime?

    @Query("SELECT * FROM animes ORDER BY createdAt DESC, id DESC LIMIT :limit")
    fun getRecentlyAdded(limit: Int = 20): Flow<List<Anime>>

    @Query("SELECT * FROM animes ORDER BY visitas DESC LIMIT :limit")
    fun getMostViewed(limit: Int = 20): Flow<List<Anime>>

    @Query("SELECT * FROM animes ORDER BY CAST(voteAverage AS REAL) DESC LIMIT :limit")
    fun getMostPopular(limit: Int = 20): Flow<List<Anime>>

    @Query("SELECT * FROM animes WHERE name LIKE '%' || :query || '%' OR nameAlternative LIKE '%' || :query || '%' OR genres LIKE '%' || :query || '%'")
    fun searchAnimes(query: String): Flow<List<Anime>>

    @Query("SELECT * FROM animes WHERE genres LIKE '%' || :genre || '%'")
    fun getAnimesByGenre(genre: String): Flow<List<Anime>>

    @Query("SELECT * FROM animes WHERE isDubbing = 1 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getDubbed(limit: Int, offset: Int): List<Anime>

    @Query("SELECT * FROM animes WHERE isDubbing = 0 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getSubbed(limit: Int, offset: Int): List<Anime>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(animes: List<Anime>)
}
