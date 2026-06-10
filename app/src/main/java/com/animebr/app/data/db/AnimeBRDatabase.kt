package com.animebr.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.animebr.app.data.model.Anime
import com.animebr.app.data.model.Download
import com.animebr.app.data.model.Episode
import com.animebr.app.data.model.Favorite
import com.animebr.app.data.model.Player
import com.animebr.app.data.model.WatchHistory

@Database(
    entities = [
        Anime::class,
        Episode::class,
        Player::class,
        WatchHistory::class,
        Favorite::class,
        Download::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnimeBRDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
}
