package com.example.ui.phone

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.api.PlayerSources
import com.example.data.api.TmdbApi
import com.example.data.model.HistoryEntry
import com.example.data.model.MediaItem
import com.example.ui.navigation.Routes
import com.example.ui.theme.LocalStreambertColors
import com.example.ui.components.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val heroMedia by viewModel.heroMedia.collectAsState()
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val trendingTv by viewModel.trendingTv.collectAsState()
    val topRated by viewModel.topRated.collectAsState()
    val recommended by viewModel.recommended.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val anime by viewModel.anime.collectAsState()
    val punjabiMovies by viewModel.punjabiMovies.collectAsState()
    val indianMovies by viewModel.indianMovies.collectAsState()

    val colors = LocalStreambertColors.current

    // Trigger reload whenever screen is entered to update continue watching progress
    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        if (isLoading) {
            val brush = shimmerBrush()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ShimmerHeroBanner(shimmerBrush = brush)
                Spacer(modifier = Modifier.height(16.dp))
                ShimmerContentRow(shimmerBrush = brush)
                Spacer(modifier = Modifier.height(16.dp))
                ShimmerContentRow(shimmerBrush = brush)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Hero Banner
                heroMedia?.let { hero ->
                    HeroBanner(hero = hero, navController = navController)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Continue Watching Row
                if (continueWatching.isNotEmpty()) {
                    ContinueWatchingSection(
                        items = continueWatching,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Recommended Section
                if (recommended.isNotEmpty()) {
                    ContentSection(
                        title = "Recommended for You",
                        items = recommended,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Trending Movies Section
                if (trendingMovies.isNotEmpty()) {
                    ContentSection(
                        title = "Trending Movies",
                        items = trendingMovies,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. Trending TV Shows Section
                if (trendingTv.isNotEmpty()) {
                    ContentSection(
                        title = "Trending Series",
                        items = trendingTv,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6. Top Rated Section
                if (topRated.isNotEmpty()) {
                    ContentSection(
                        title = "Top Rated Selection",
                        items = topRated,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 7. Anime Section
                if (anime.isNotEmpty()) {
                    ContentSection(
                        title = "Anime Series & Movies",
                        items = anime,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 8. Punjabi Section
                if (punjabiMovies.isNotEmpty()) {
                    ContentSection(
                        title = "Punjabi Hits",
                        items = punjabiMovies,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 9. Indian Section
                if (indianMovies.isNotEmpty()) {
                    ContentSection(
                        title = "Indian Cinema",
                        items = indianMovies,
                        navController = navController
                    )
                    Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
                } else {
                    Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
                }
            }
        }
    }
}

@Composable
fun HeroBanner(
    hero: MediaItem,
    navController: NavController
) {
    val colors = LocalStreambertColors.current
    val backdropUrl = TmdbApi.imgUrl(hero.backdropPath, "original")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .testTag("hero_banner")
    ) {
        // Backdrop image
        AsyncImage(
            model = backdropUrl,
            contentDescription = hero.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            colors.bg.copy(alpha = 0.5f),
                            colors.bg
                        )
                    )
                )
        )

        // Hero content aligned bottom left
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = hero.displayTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val rating = "%.1f".format(hero.voteAverage)
                val type = if (hero.isTv) "Series" else "Movie"
                
                Text(
                    text = "★ $rating",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = hero.displayYear,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .background(colors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = type.uppercase(),
                        color = colors.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = hero.overview,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        // Play action: route to detail page
                        navController.navigate(Routes.detail(hero.id, hero.mediaType))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hero_play_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch Now", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        navController.navigate(Routes.detail(hero.id, hero.mediaType))
                    },
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hero_info_button")
                ) {
                    Icon(Icons.Filled.Info, contentDescription = "More Info", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Details", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ContentSection(
    title: String,
    items: List<MediaItem>,
    navController: NavController
) {
    val colors = LocalStreambertColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                MediaCard(item = item, navController = navController)
            }
        }
    }
}

@Composable
fun MediaCard(
    item: MediaItem,
    navController: NavController
) {
    val colors = LocalStreambertColors.current
    val posterUrl = TmdbApi.imgUrl(item.posterPath, "w342")
    val isAnime = PlayerSources.isAnimeContent(item.genreIds, item.originalLanguage, item.originCountry)

    Column(
        modifier = Modifier
            .width(130.dp)
            .scaleOnPress()
            .clickable {
                navController.navigate(Routes.detail(item.id, item.mediaType))
            }
            .testTag("media_card_${item.id}")
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(195.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface2)
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = item.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlaid badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (item.isUnreleased) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SOON",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isAnime) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7C3AED), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ANIME",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(colors.bg.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.isTv) "TV" else "HD",
                            color = colors.text,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.displayTitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.text,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.displayYear,
            style = MaterialTheme.typography.labelSmall,
            color = colors.text2
        )
    }
}

@Composable
fun ContinueWatchingSection(
    items: List<HistoryEntry>,
    navController: NavController
) {
    val colors = LocalStreambertColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Continue Watching",
            style = MaterialTheme.typography.titleLarge,
            color = colors.text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { entry ->
                ContinueWatchingCard(entry = entry, navController = navController)
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    entry: HistoryEntry,
    navController: NavController
) {
    val colors = LocalStreambertColors.current
    val posterUrl = TmdbApi.imgUrl(entry.posterPath, "w342")

    Column(
        modifier = Modifier
            .width(130.dp)
            .scaleOnPress()
            .clickable {
                navController.navigate(Routes.detail(entry.id, entry.mediaType))
            }
            .testTag("continue_card_${entry.id}")
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(195.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface2)
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Progress bar at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color.Gray.copy(alpha = 0.5f))
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(entry.progress.coerceIn(0f, 1f))
                        .background(colors.accent)
                )
            }

            // Play icon overlay in center
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .align(Alignment.Center)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Resume",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = entry.title,
            style = MaterialTheme.typography.bodySmall,
            color = colors.text,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val detailText = if (entry.mediaType == "tv") {
            "S${entry.season} E${entry.episode}"
        } else {
            "Movie"
        }

        Text(
            text = detailText,
            style = MaterialTheme.typography.labelSmall,
            color = colors.text2
        )
    }
}
