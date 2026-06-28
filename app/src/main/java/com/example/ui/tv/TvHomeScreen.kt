package com.example.ui.tv

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.api.TmdbApi
import com.example.data.model.MediaItem
import com.example.ui.navigation.Routes
import com.example.ui.phone.HomeViewModel
import com.example.ui.phone.LibraryViewModel
import com.example.ui.components.*

private fun com.example.data.model.HistoryEntry.toMediaItem(): MediaItem {
    return MediaItem(
        id = this.id,
        title = this.title,
        posterPath = this.posterPath,
        mediaType = this.mediaType,
        season = this.season,
        episode = this.episode,
        episodeName = this.episodeName,
        watchedAt = this.watchedAt
    )
}

private fun com.example.data.model.SavedItem.toMediaItem(): MediaItem {
    return MediaItem(
        id = this.id,
        title = this.title,
        posterPath = this.posterPath,
        mediaType = this.mediaType,
        voteAverage = this.voteAverage,
        releaseDate = this.year
    )
}

@Composable
fun TvHomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val heroMedia by viewModel.heroMedia.collectAsState()
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val trendingTv by viewModel.trendingTv.collectAsState()
    val anime by viewModel.anime.collectAsState()
    val punjabiMovies by viewModel.punjabiMovies.collectAsState()
    val indianMovies by viewModel.indianMovies.collectAsState()
    
    val continueWatching by libraryViewModel.continueWatching.collectAsState()
    val watchlist by libraryViewModel.watchlist.collectAsState()

    LaunchedEffect(Unit) {
        libraryViewModel.loadLibraryData()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // 1. Left Sidebar Navigation
        TvSidebar(navController = navController, activeRoute = Routes.HOME)

        // 2. Main Content
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(start = 8.dp, top = 16.dp, end = 16.dp)
        ) {
            if (isLoading) {
                val brush = shimmerBrush()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ShimmerHeroBanner(shimmerBrush = brush)
                    Spacer(modifier = Modifier.height(24.dp))
                    ShimmerContentRow(shimmerBrush = brush)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero Feature Highlight
                    heroMedia?.let { hero ->
                        TvHeroSection(hero = hero, navController = navController)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Continue Watching
                    if (continueWatching.isNotEmpty()) {
                        TvContentSection(
                            title = "Continue Watching",
                            items = continueWatching.map { it.toMediaItem() },
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // My Watchlist
                    if (watchlist.isNotEmpty()) {
                        TvContentSection(
                            title = "My Watchlist",
                            items = watchlist.map { it.toMediaItem() },
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Trending Movies
                    if (trendingMovies.isNotEmpty()) {
                        TvContentSection(
                            title = "Trending Movies",
                            items = trendingMovies,
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Trending Series
                    if (trendingTv.isNotEmpty()) {
                        TvContentSection(
                            title = "Trending Series",
                            items = trendingTv,
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Anime Section
                    if (anime.isNotEmpty()) {
                        TvContentSection(
                            title = "Anime Series & Movies",
                            items = anime,
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Punjabi Section
                    if (punjabiMovies.isNotEmpty()) {
                        TvContentSection(
                            title = "Punjabi Hits",
                            items = punjabiMovies,
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Indian Section
                    if (indianMovies.isNotEmpty()) {
                        TvContentSection(
                            title = "Indian Cinema",
                            items = indianMovies,
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    } else {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TvSidebar(
    navController: NavController,
    activeRoute: String
) {
    val menuItems = listOf(
        Triple(Routes.HOME, Icons.Default.Home, "Home"),
        Triple(Routes.SEARCH, Icons.Default.Search, "Search"),
        Triple(Routes.LIBRARY, Icons.Default.VideoLibrary, "Library"),
        Triple(Routes.SETTINGS, Icons.Default.Settings, "Settings")
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(140.dp)
            .background(Color(0xFF111111))
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo
        Text(
            text = "STREAMBERT",
            color = Color(0xFFE50914),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Menu Items
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            menuItems.forEach { (route, icon, label) ->
                val isSelected = activeRoute == route
                var focused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                focused -> Color(0xFFE50914).copy(alpha = 0.15f)
                                isSelected -> Color(0xFF222222)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (focused) 1.dp else 0.dp,
                            color = if (focused) Color(0xFFE50914) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            if (route == Routes.HOME) {
                                navController.navigate(route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                navController.navigate(route)
                            }
                        }
                        .onFocusChanged { focused = it.hasFocus }
                        .focusable()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (focused || isSelected) Color(0xFFE50914) else Color(0xFF888888),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            color = if (focused || isSelected) Color.White else Color(0xFF888888),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TvHeroSection(
    hero: MediaItem,
    navController: NavController
) {
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .onFocusChanged { focused = it.hasFocus }
            .focusable()
            .clickable {
                navController.navigate(Routes.detail(hero.id, hero.mediaType))
            }
    ) {
        // Image
        AsyncImage(
            model = TmdbApi.imgUrl(hero.backdropPath, "original"),
            contentDescription = hero.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradients
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Text Info overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = hero.displayTitle,
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("★ %.1f".format(hero.voteAverage), color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(hero.displayYear, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                Text(if (hero.isTv) "Series" else "Movie", color = Color(0xFFE50914), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TvContentSection(
    title: String,
    items: List<MediaItem>,
    navController: NavController
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                TvMediaCard(item = item, navController = navController)
            }
        }
    }
}

@Composable
fun TvMediaCard(
    item: MediaItem,
    navController: NavController
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(160.dp)
            .scaleOnFocus()
            .clip(RoundedCornerShape(8.dp))
            .background(if (focused) Color(0xFF1E1E1E) else Color.Transparent)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .onFocusChanged { focused = it.hasFocus }
            .focusable()
            .clickable {
                navController.navigate(Routes.detail(item.id, item.mediaType))
            }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            AsyncImage(
                model = TmdbApi.imgUrl(item.posterPath, "w342"),
                contentDescription = item.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic bottom card cover when focused
            if (focused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.displayTitle,
            color = if (focused) Color.White else Color(0xFFCCCCCC),
            fontSize = 13.sp,
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.displayYear,
            color = Color(0xFF888888),
            fontSize = 11.sp
        )
    }
}
