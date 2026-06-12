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
        val dbFile = DatabaseDecryptor.getDecryptedDbFile(context)
        val dbPath = context.getDatabasePath("animebr.db")

        return Room.databaseBuilder(
            context,
            AnimeBRDatabase::class.java,
            "animebr.db"
        )
            .apply {
                // Only use createFromFile on first install (DB doesn't exist yet)
                if (!dbPath.exists()) {
                    createFromFile(dbFile)
                }
            }
            // Use addMigrations with empty migration to preserve user data on version bump
            .addMigrations(object : androidx.room.migration.Migration(1, 2) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // No schema changes - just bump version without destroying data
                }
            })
            .fallbackToDestructiveMigrationOnDowngrade()
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
