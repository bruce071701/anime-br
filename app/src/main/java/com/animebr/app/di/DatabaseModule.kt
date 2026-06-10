package com.animebr.app.di

import android.content.Context
import androidx.room.Room
import com.animebr.app.data.db.AnimeBRDatabase
import com.animebr.app.data.db.AnimeDao
import com.animebr.app.data.db.DatabaseDecryptor
import com.animebr.app.data.db.DownloadDao
import com.animebr.app.data.db.EpisodeDao
import com.animebr.app.data.db.FavoriteDao
import com.animebr.app.data.db.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnimeBRDatabase {
        // Decrypt the compressed+encrypted db from assets on first run
        val dbFile = DatabaseDecryptor.getDecryptedDbFile(context)

        return Room.databaseBuilder(
            context,
            AnimeBRDatabase::class.java,
            "animebr.db"
        )
            .createFromFile(dbFile)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAnimeDao(database: AnimeBRDatabase): AnimeDao = database.animeDao()

    @Provides
    fun provideEpisodeDao(database: AnimeBRDatabase): EpisodeDao = database.episodeDao()

    @Provides
    fun provideWatchHistoryDao(database: AnimeBRDatabase): WatchHistoryDao = database.watchHistoryDao()

    @Provides
    fun provideFavoriteDao(database: AnimeBRDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideDownloadDao(database: AnimeBRDatabase): DownloadDao = database.downloadDao()
}
