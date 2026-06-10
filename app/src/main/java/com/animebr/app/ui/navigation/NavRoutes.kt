package com.animebr.app.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Splash : NavRoutes("splash")
    data object Home : NavRoutes("home")
    data object Search : NavRoutes("search")
    data object AllAnimes : NavRoutes("all_animes")
    data object Genres : NavRoutes("genres")
    data object Popular : NavRoutes("popular")
    data object Movies : NavRoutes("movies")
    data object Favorites : NavRoutes("favorites")
    data object History : NavRoutes("history")
    data object Downloads : NavRoutes("downloads")
    data object Me : NavRoutes("me")
    data object Dublado : NavRoutes("dubsub/dublado")
    data object Legendado : NavRoutes("dubsub/legendado")
    data object Detail : NavRoutes("detail/{animeId}") {
        fun createRoute(animeId: Int) = "detail/$animeId"
    }
    data object EpisodeList : NavRoutes("episode_list/{animeId}") {
        fun createRoute(animeId: Int) = "episode_list/$animeId"
    }
    data object EpisodeSelect : NavRoutes("episode_select/{animeId}/{episodeId}") {
        fun createRoute(animeId: Int, episodeId: Int) = "episode_select/$animeId/$episodeId"
    }
    data object Player : NavRoutes("player/{episodeId}/{sourceId}") {
        fun createRoute(episodeId: Int, sourceId: Int) = "player/$episodeId/$sourceId"
    }
    data object PlayerAuto : NavRoutes("player_auto/{episodeId}") {
        fun createRoute(episodeId: Int) = "player_auto/$episodeId"
    }
    data object PlayerLocal : NavRoutes("player_local/{filePath}") {
        fun createRoute(filePath: String) = "player_local/${java.net.URLEncoder.encode(filePath, "UTF-8")}"
    }
    data object Genre : NavRoutes("genre/{genreName}") {
        fun createRoute(genreName: String) = "genre/$genreName"
    }
}
