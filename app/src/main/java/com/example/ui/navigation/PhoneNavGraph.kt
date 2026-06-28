package com.example.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.phone.*

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail/{id}/{type}"
    const val PLAYER = "player?url={url}&type={type}&tmdbId={tmdbId}&season={season}&episode={episode}&title={title}"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val DOWNLOADS = "downloads"
    const val SETUP = "setup"

    fun detail(id: Int, type: String): String {
        return "detail/$id/$type"
    }

    fun playerArgs(
        url: String,
        type: String,
        tmdbId: Int,
        season: Int? = null,
        episode: Int? = null,
        title: String
    ): String {
        val encodedUrl = Uri.encode(Uri.encode(url))
        val encodedTitle = Uri.encode(Uri.encode(title))
        val s = season?.toString() ?: ""
        val e = episode?.toString() ?: ""
        return "player?url=$encodedUrl&type=$type&tmdbId=$tmdbId&season=$s&episode=$e&title=$encodedTitle"
    }
}

@Composable
fun PhoneNavHost(
    navController: NavHostController,
    startDestination: String,
    onSettingsChanged: () -> Unit,
    onSetupComplete: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }
        
        composable(Routes.SEARCH) {
            SearchScreen(navController = navController)
        }
        
        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val type = backStackEntry.arguments?.getString("type") ?: "movie"
            DetailScreen(navController = navController, mediaId = id, mediaType = type)
        }
        
        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType },
                navArgument("tmdbId") { type = NavType.IntType },
                navArgument("season") { type = NavType.StringType; defaultValue = "" },
                navArgument("episode") { type = NavType.StringType; defaultValue = "" },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "movie"
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            val seasonStr = backStackEntry.arguments?.getString("season") ?: ""
            val episodeStr = backStackEntry.arguments?.getString("episode") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""

            val season = seasonStr.ifBlank { null }?.toIntOrNull()
            val episode = episodeStr.ifBlank { null }?.toIntOrNull()

            PlayerScreen(
                navController = navController,
                streamUrl = url,
                mediaType = type,
                tmdbId = tmdbId,
                season = season,
                episode = episode,
                title = title
            )
        }
        
        composable(Routes.LIBRARY) {
            LibraryScreen(navController = navController)
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController, onSettingsChanged = onSettingsChanged)
        }
        
        composable(Routes.DOWNLOADS) {
            DownloadsScreen(navController = navController)
        }

        composable(Routes.SETUP) {
            SetupScreen(onSetupComplete = onSetupComplete)
        }
    }
}
