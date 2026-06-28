package com.example.ui.phone

import android.app.Application
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.api.AniListApi
import com.example.data.api.PlayerSources
import com.example.data.api.TmdbApi
import com.example.data.model.*
import com.example.data.repository.MediaRepository
import com.example.ui.navigation.Routes
import com.example.ui.theme.LocalStreambertColors
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    var detail by mutableStateOf<DetailCommon?>(null)
        private set
    var episodes by mutableStateOf<List<TvEpisode>>(emptyList())
        private set
    var trailers by mutableStateOf<List<TmdbVideo>>(emptyList())
        private set
    var isSaved by mutableStateOf(false)
        private set
    var anilistData by mutableStateOf<AniListMedia?>(null)
        private set
    var progressMap by mutableStateOf<Map<String, Float>>(emptyMap())
        private set
    var watchedMap by mutableStateOf<Map<String, Boolean>>(emptyMap())
        private set
    var isLoading by mutableStateOf(true)
        private set

    fun load(mediaId: Int, mediaType: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                isSaved = MediaRepository.isSaved(getApplication(), mediaId, mediaType)
                progressMap = com.example.data.local.Prefs.getWatchProgress(getApplication())
                watchedMap = com.example.data.local.Prefs.getWatched(getApplication())

                if (mediaType == "anilist") {
                    val animeData = AniListApi.fetchAnilistDataById(mediaId)
                    if (animeData != null) {
                        anilistData = animeData
                        val finalTitle = animeData.title?.english ?: animeData.title?.romaji ?: animeData.title?.native ?: "Anime"
                        detail = com.example.data.model.AniListDetail(
                            id = animeData.id,
                            title = finalTitle,
                            posterPath = animeData.coverImage?.large ?: animeData.coverImage?.medium ?: animeData.coverImage?.extraLarge,
                            backdropPath = animeData.bannerImage,
                            overview = AniListApi.cleanDescription(animeData.description),
                            year = animeData.seasonYear?.toString() ?: "",
                            voteAverage = (animeData.averageScore / 10f),
                            genres = animeData.genres.mapIndexed { idx, name -> Genre(id = idx, name = name) },
                            mediaType = "anilist"
                        )
                        val totalEps = animeData.episodes
                        val defaultEps = if (totalEps > 0) totalEps else 1
                        episodes = (1..defaultEps).map { epNum ->
                            TvEpisode(
                                id = animeData.id * 1000 + epNum,
                                seasonNumber = 1,
                                episodeNumber = epNum,
                                name = "Episode $epNum",
                                overview = "Watch Episode $epNum of $finalTitle",
                                showId = animeData.id
                            )
                        }
                    }
                } else if (mediaType == "tv") {
                    val tvDetails = MediaRepository.getTvDetail(mediaId)
                    detail = tvDetails
                    
                    // Fetch trailers
                    trailers = tvDetails.videos?.results?.filter { it.site.lowercase() == "youtube" } ?: emptyList()

                    // Fetch first season episodes (typically season 1)
                    val sNumber = tvDetails.seasons.firstOrNull { it.seasonNumber > 0 }?.seasonNumber ?: 1
                    val seasonDetail = MediaRepository.getTvSeasonDetail(mediaId, sNumber)
                    episodes = seasonDetail.episodes

                    // Try anime enrichment via AniList
                    val isAnime = PlayerSources.isAnimeContent(
                        tvDetails.genres.map { it.id },
                        tvDetails.originalLanguage,
                        tvDetails.originCountry
                    )
                    if (isAnime) {
                        val animeData = AniListApi.fetchAnilistData(tvDetails.name, "tv")
                        anilistData = animeData
                    }
                } else {
                    val movieDetails = MediaRepository.getMovieDetail(mediaId)
                    detail = movieDetails
                    
                    // Fetch trailers
                    trailers = movieDetails.videos?.results?.filter { it.site.lowercase() == "youtube" } ?: emptyList()

                    // Try anime enrichment via AniList
                    val isAnime = PlayerSources.isAnimeContent(
                        movieDetails.genres.map { it.id },
                        movieDetails.originalLanguage,
                        movieDetails.originCountry
                    )
                    if (isAnime) {
                        val animeData = AniListApi.fetchAnilistData(movieDetails.title, "movie")
                        anilistData = animeData
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Failed to load movie details", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleSave() {
        val currentDetail = detail ?: return
        viewModelScope.launch {
            val savedItem = SavedItem(
                id = currentDetail.id,
                title = currentDetail.displayTitle,
                posterPath = currentDetail.posterPath,
                mediaType = currentDetail.mediaType,
                voteAverage = currentDetail.voteAverage,
                year = currentDetail.displayYear
            )
            MediaRepository.toggleSaved(getApplication(), savedItem)
            isSaved = !isSaved
        }
    }
}

@Composable
fun DetailScreen(
    navController: NavController,
    mediaId: Int,
    mediaType: String,
    viewModel: DetailViewModel = viewModel()
) {
    val colors = LocalStreambertColors.current
    val context = LocalContext.current
    val detail = viewModel.detail
    val episodes = viewModel.episodes
    val trailers = viewModel.trailers
    val isSaved = viewModel.isSaved
    val anilistData = viewModel.anilistData
    val progressMap = viewModel.progressMap
    val watchedMap = viewModel.watchedMap
    val isLoading = viewModel.isLoading

    LaunchedEffect(mediaId, mediaType) {
        viewModel.load(mediaId, mediaType)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        if (isLoading || detail == null) {
            CircularProgressIndicator(
                color = colors.accent,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("detail_loading")
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Backdrop header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        AsyncImage(
                            model = TmdbApi.imgUrl(detail.backdropPath, "original"),
                            contentDescription = detail.displayTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Fade overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            colors.bg.copy(alpha = 0.6f),
                                            colors.bg
                                        )
                                    )
                                )
                        )

                        // Custom Back Button with shadow backplate
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }

                // 2. Poster, Title and details row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-45).dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Poster Thumbnail with dynamic shadow border
                        AsyncImage(
                            model = TmdbApi.imgUrl(detail.posterPath, "w342"),
                            contentDescription = detail.displayTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(115.dp)
                                .height(172.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, colors.border, RoundedCornerShape(8.dp))
                                .background(colors.surface2)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = detail.displayTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.text,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Metadata indicators
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "★ %.1f".format(detail.voteAverage),
                                    color = Color(0xFFFFD700),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = detail.displayYear,
                                    color = colors.text2,
                                    fontSize = 13.sp
                                )

                                val runtimeText = if (detail is MovieDetail) {
                                    "${detail.runtime} min"
                                } else if (detail is TvDetail) {
                                    "${detail.numberOfSeasons} Seasons"
                                } else ""

                                if (runtimeText.isNotEmpty()) {
                                    Text(
                                        text = runtimeText,
                                        color = colors.text2,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Genres chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                detail.genres.take(3).forEach { genre ->
                                    Box(
                                        modifier = Modifier
                                            .background(colors.surface2, RoundedCornerShape(12.dp))
                                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = genre.name,
                                            color = colors.text2,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. CTA Buttons Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-30).dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                // Launch watch source URL
                                viewModel.viewModelScope.launch {
                                    val source = if (mediaType == "anilist") {
                                        "videasy"
                                    } else {
                                        com.example.data.local.Prefs.getPlayerSource(context)
                                            .ifEmpty { PlayerSources.NON_ANIME_DEFAULT }
                                    }
                                    
                                    val streamUrl = PlayerSources.getSourceUrl(
                                        sourceId = source,
                                        type = mediaType,
                                        tmdbId = mediaId,
                                        accentColor = "#e50914"
                                    )
                                    navController.navigate(
                                        Routes.playerArgs(
                                            url = streamUrl,
                                            type = mediaType,
                                            tmdbId = mediaId,
                                            title = detail.displayTitle
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("play_button")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Save Watchlist Button
                        IconButton(
                            onClick = { viewModel.toggleSave() },
                            modifier = Modifier
                                .size(46.dp)
                                .background(colors.surface2, RoundedCornerShape(8.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .testTag("watchlist_button")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (isSaved) colors.accent else colors.text2
                            )
                        }

                        // Trailer Button (External Youtube intent)
                        if (trailers.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val trailerKey = trailers.first().key
                                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$trailerKey"))
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$trailerKey"))
                                    try {
                                        context.startActivity(appIntent)
                                    } catch (ex: Exception) {
                                        context.startActivity(webIntent)
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(colors.surface2, RoundedCornerShape(8.dp))
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                    .testTag("trailer_button")
                            ) {
                                Icon(
                                    Icons.Default.Tv,
                                    contentDescription = "Trailer",
                                    tint = colors.text2
                                )
                            }
                        }
                    }
                }

                // 4. Content Overview description
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-16).dp)
                    ) {
                        Text(
                            "Overview",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = detail.overview.ifEmpty { "No description available." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.text2,
                            lineHeight = 22.sp
                        )
                    }
                }

                // 5. AniList Integration Information section (Anime content)
                if (anilistData != null) {
                    item {
                        val cleanedDesc = AniListApi.cleanDescription(anilistData.description)
                        if (cleanedDesc.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Anime Info (AniList)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = cleanedDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.text2,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // 6. Seasons and Episodes Picker (Series content)
                if ((mediaType == "tv" || mediaType == "anilist") && episodes.isNotEmpty()) {
                    item {
                        Text(
                            "Episodes",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    items(episodes) { episode ->
                        val key = "tv_${mediaId}_s${episode.seasonNumber}_e${episode.episodeNumber}"
                        val progress = progressMap[key] ?: 0f
                        val isFullyWatched = watchedMap[key] ?: false

                        EpisodeRow(
                            episode = episode,
                            progress = progress,
                            isWatched = isFullyWatched,
                            onClick = {
                                viewModel.viewModelScope.launch {
                                    val source = if (mediaType == "anilist") {
                                        "videasy"
                                    } else {
                                        com.example.data.local.Prefs.getPlayerSource(context)
                                            .ifEmpty { PlayerSources.NON_ANIME_DEFAULT }
                                    }
                                    
                                    val streamUrl = PlayerSources.getSourceUrl(
                                        sourceId = source,
                                        type = mediaType,
                                        tmdbId = mediaId,
                                        season = episode.seasonNumber,
                                        episode = episode.episodeNumber,
                                        accentColor = "#e50914"
                                    )
                                    navController.navigate(
                                        Routes.playerArgs(
                                            url = streamUrl,
                                            type = mediaType,
                                            tmdbId = mediaId,
                                            season = episode.seasonNumber,
                                            episode = episode.episodeNumber,
                                            title = if (mediaType == "anilist") "${detail.displayTitle} - Episode ${episode.episodeNumber}" else "${detail.displayTitle} - S${episode.seasonNumber}E${episode.episodeNumber}"
                                        )
                                    )
                                }
                            }
                        )
                        HorizontalDivider(
                            color = colors.border,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Collection items (if present)
                if (detail is MovieDetail && detail.belongsToCollection != null) {
                    item {
                        val col = detail.belongsToCollection
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Part of Collection",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.text,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surface2, RoundedCornerShape(8.dp))
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = TmdbApi.imgUrl(col.posterPath, "w185"),
                                    contentDescription = col.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp, 90.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.surface3)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = col.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.text,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Padding spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun EpisodeRow(
    episode: TvEpisode,
    progress: Float,
    isWatched: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalStreambertColors.current
    val stillUrl = TmdbApi.imgUrl(episode.stillPath, "w342")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Still thumbnail
        Box(
            modifier = Modifier
                .size(110.dp, 68.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surface2)
        ) {
            AsyncImage(
                model = stillUrl,
                contentDescription = episode.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Watched completion overlay checkmark
            if (isWatched) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Watched",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                    )
                }
            } else if (progress in 0.02f..0.98f) {
                // Horizontal watch percentage indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Gray.copy(alpha = 0.5f))
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(colors.accent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "EPISODE ${episode.episodeNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = episode.name.ifEmpty { "Episode ${episode.episodeNumber}" },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = episode.airDate ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = colors.text2
            )
        }
    }
}
