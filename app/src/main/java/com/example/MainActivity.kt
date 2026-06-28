package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.example.data.local.Prefs
import com.example.data.repository.MediaRepository
import com.example.ui.navigation.Routes
import com.example.ui.phone.*
import com.example.ui.theme.LocalStreambertColors
import com.example.ui.theme.StreambertTheme
import com.example.ui.tv.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var setupDone by remember { mutableStateOf<Boolean?>(null) }
            var isTvMode by remember { mutableStateOf(false) }
            var themeStyle by remember { mutableStateOf("dark") }
            var accentColorHex by remember { mutableStateOf("#e50914") }
            var keyToRefresh by remember { mutableStateOf(0) }

            // Dynamic setup settings loading
            LaunchedEffect(keyToRefresh) {
                setupDone = Prefs.isSetupDone(context)
                isTvMode = Prefs.isTvMode(context)
                themeStyle = Prefs.getTheme(context)
                accentColorHex = Prefs.getAccentColor(context)
                
                // Initialize TMDB Client
                MediaRepository.configureApi(context)
            }

            if (setupDone == null) {
                // Initial Loading Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Red)
                }
            } else {
                StreambertTheme(
                    themeId = themeStyle,
                    accentHex = accentColorHex
                ) {
                    if (setupDone == false) {
                        SetupScreen(
                            onSetupComplete = {
                                keyToRefresh++
                            }
                        )
                    } else {
                        if (isTvMode) {
                            TvAppNavigation(
                                onSettingsChanged = { keyToRefresh++ }
                            )
                        } else {
                            PhoneAppNavigation(
                                onSettingsChanged = { keyToRefresh++ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneAppNavigation(
    onSettingsChanged: () -> Unit
) {
    val navController = rememberNavController()
    val colors = LocalStreambertColors.current

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Hide bottom bar on Player Screen
            if (currentRoute != null && !currentRoute.startsWith(Routes.PLAYER)) {
                NavigationBar(
                    containerColor = colors.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.text3,
                            unselectedTextColor = colors.text3,
                            indicatorColor = colors.surface2
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = currentRoute == Routes.SEARCH,
                        onClick = {
                            navController.navigate(Routes.SEARCH) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.text3,
                            unselectedTextColor = colors.text3,
                            indicatorColor = colors.surface2
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.VideoLibrary, contentDescription = "Library") },
                        label = { Text("Library") },
                        selected = currentRoute == Routes.LIBRARY,
                        onClick = {
                            navController.navigate(Routes.LIBRARY) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.text3,
                            unselectedTextColor = colors.text3,
                            indicatorColor = colors.surface2
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.FileDownload, contentDescription = "Downloads") },
                        label = { Text("Offline") },
                        selected = currentRoute == Routes.DOWNLOADS,
                        onClick = {
                            navController.navigate(Routes.DOWNLOADS) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.text3,
                            unselectedTextColor = colors.text3,
                            indicatorColor = colors.surface2
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = {
                            navController.navigate(Routes.SETTINGS) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.text3,
                            unselectedTextColor = colors.text3,
                            indicatorColor = colors.surface2
                        )
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController = navController)
            }
            composable(Routes.SEARCH) {
                SearchScreen(navController = navController)
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(navController = navController)
            }
            composable(Routes.DOWNLOADS) {
                DownloadsScreen(navController = navController)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    onSettingsChanged = onSettingsChanged
                )
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
                val urlRaw = backStackEntry.arguments?.getString("url") ?: ""
                val url = android.net.Uri.decode(urlRaw)
                val type = backStackEntry.arguments?.getString("type") ?: ""
                val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
                val seasonStr = backStackEntry.arguments?.getString("season") ?: ""
                val episodeStr = backStackEntry.arguments?.getString("episode") ?: ""
                val titleRaw = backStackEntry.arguments?.getString("title") ?: ""
                val title = android.net.Uri.decode(titleRaw)

                val s = seasonStr.ifBlank { null }?.toIntOrNull()
                val e = episodeStr.ifBlank { null }?.toIntOrNull()

                PlayerScreen(
                    navController = navController,
                    streamUrl = url,
                    mediaType = type,
                    tmdbId = tmdbId,
                    season = s,
                    episode = e,
                    title = title
                )
            }
            composable(Routes.SETUP) {
                SetupScreen(
                    onSetupComplete = onSettingsChanged
                )
            }
        }
    }
}

@Composable
fun TvAppNavigation(
    onSettingsChanged: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        }
    ) {
        composable(Routes.HOME) {
            TvHomeScreen(navController = navController)
        }
        composable(Routes.SEARCH) {
            TvSearchScreen(navController = navController)
        }
        composable(Routes.LIBRARY) {
            TvLibraryScreen(navController = navController)
        }
        composable(Routes.SETTINGS) {
            TvSettingsScreen(
                navController = navController
            )
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
            TvDetailScreen(navController = navController, mediaId = id, mediaType = type)
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
            val urlRaw = backStackEntry.arguments?.getString("url") ?: ""
            val url = android.net.Uri.decode(urlRaw)
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            val seasonStr = backStackEntry.arguments?.getString("season") ?: ""
            val episodeStr = backStackEntry.arguments?.getString("episode") ?: ""
            val titleRaw = backStackEntry.arguments?.getString("title") ?: ""
            val title = android.net.Uri.decode(titleRaw)

            val s = seasonStr.ifBlank { null }?.toIntOrNull()
            val e = episodeStr.ifBlank { null }?.toIntOrNull()

            TvPlayerScreen(
                navController = navController,
                streamUrl = url,
                mediaType = type,
                tmdbId = tmdbId,
                season = s,
                episode = e,
                title = title
            )
        }
        composable(Routes.SETUP) {
            SetupScreen(
                onSetupComplete = onSettingsChanged
            )
        }
    }
}
