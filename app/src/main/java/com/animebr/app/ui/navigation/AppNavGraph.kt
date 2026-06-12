package com.animebr.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.animebr.app.ui.allanimes.AllAnimesScreen
import com.animebr.app.ui.detail.DetailScreen
import com.animebr.app.ui.episodes.EpisodeListScreen
import com.animebr.app.ui.episode.EpisodeSelectScreen
import com.animebr.app.ui.favorites.FavoritesScreen
import com.animebr.app.ui.genres.GenresScreen
import com.animebr.app.ui.genres.GenreDetailScreen
import com.animebr.app.ui.history.HistoryScreen
import com.animebr.app.ui.home.HomeScreen
import com.animebr.app.ui.movies.MoviesScreen
import com.animebr.app.ui.player.PlayerScreen
import com.animebr.app.ui.popular.PopularScreen
import com.animebr.app.ui.rating.RatingManager
import com.animebr.app.ui.search.SearchScreen
import com.animebr.app.ui.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    ratingManager: RatingManager,
    onSplashFinished: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onTimeout = {
                    onSplashFinished()
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Home.route) {
            HomeScreen(
                navController = navController
            )
        }

        composable(NavRoutes.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                }
            )
        }

        composable(NavRoutes.AllAnimes.route) {
            AllAnimesScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                }
            )
        }

        composable(NavRoutes.Genres.route) {
            GenresScreen(
                onBackClick = { navController.popBackStack() },
                onGenreClick = { genre ->
                    navController.navigate(NavRoutes.Genre.createRoute(genre))
                }
            )
        }

        composable(
            route = NavRoutes.Genre.route,
            arguments = listOf(navArgument("genreName") { type = NavType.StringType })
        ) {
            GenreDetailScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                }
            )
        }

        composable(NavRoutes.Popular.route) {
            PopularScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                }
            )
        }

        composable(NavRoutes.Movies.route) {
            MoviesScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                }
            )
        }

        composable(NavRoutes.Favorites.route) {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                }
            )
        }

        composable(NavRoutes.History.route) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                },
                onEpisodeClick = { animeId, episodeId ->
                    navController.navigate(NavRoutes.EpisodeSelect.createRoute(animeId, episodeId))
                }
            )
        }

        composable(NavRoutes.Downloads.route) {
            com.animebr.app.ui.downloads.DownloadsScreen(
                onBackClick = { navController.popBackStack() },
                onPlayClick = { filePath ->
                    navController.navigate(NavRoutes.PlayerLocal.createRoute(filePath))
                }
            )
        }

        composable(NavRoutes.Me.route) {
            com.animebr.app.ui.me.MeScreen(
                onBackClick = { navController.popBackStack() },
                onFavoritesClick = { navController.navigate(NavRoutes.Favorites.route) },
                onHistoryClick = { navController.navigate(NavRoutes.History.route) },
                onDownloadsClick = { navController.navigate(NavRoutes.Downloads.route) }
            )
        }

        composable("dubsub/{type}") {
            com.animebr.app.ui.dubsub.DubSubScreen(
                onBackClick = { navController.popBackStack() },
                onAnimeClick = { animeId ->
                    navController.navigate(NavRoutes.Detail.createRoute(animeId))
                }
            )
        }

        composable(
            route = NavRoutes.Detail.route,
            arguments = listOf(navArgument("animeId") { type = NavType.IntType })
        ) {
            DetailScreen(
                onBackClick = { navController.popBackStack() },
                onPlayClick = { animeId ->
                    navController.navigate(NavRoutes.EpisodeList.createRoute(animeId))
                },
                onGenreClick = { genre ->
                    navController.navigate(NavRoutes.Genre.createRoute(genre))
                }
            )
        }

        composable(
            route = NavRoutes.EpisodeList.route,
            arguments = listOf(navArgument("animeId") { type = NavType.IntType })
        ) {
            EpisodeListScreen(
                onBackClick = { navController.popBackStack() },
                onEpisodeClick = { animeId, episodeId ->
                    navController.navigate(NavRoutes.EpisodeSelect.createRoute(animeId, episodeId))
                }
            )
        }

        composable(
            route = NavRoutes.EpisodeSelect.route,
            arguments = listOf(
                navArgument("animeId") { type = NavType.IntType },
                navArgument("episodeId") { type = NavType.IntType }
            )
        ) {
            EpisodeSelectScreen(
                onBackClick = { navController.popBackStack() },
                onSourceClick = { episodeId, sourceId ->
                    navController.navigate(NavRoutes.Player.createRoute(episodeId, sourceId))
                },
                onGoToDownloads = { navController.navigate(NavRoutes.Downloads.route) },
                ratingManager = ratingManager
            )
        }

        composable(
            route = NavRoutes.Player.route,
            arguments = listOf(
                navArgument("episodeId") { type = NavType.IntType },
                navArgument("sourceId") { type = NavType.IntType }
            )
        ) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.PlayerLocal.route,
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val filePath = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath") ?: "",
                "UTF-8"
            )
            com.animebr.app.ui.player.LocalPlayerScreen(
                filePath = filePath,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
