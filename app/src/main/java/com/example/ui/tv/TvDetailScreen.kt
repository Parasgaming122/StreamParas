package com.example.ui.tv

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.data.api.AniListApi
import com.example.data.api.PlayerSources
import com.example.data.api.TmdbApi
import com.example.data.model.*
import com.example.ui.navigation.Routes
import com.example.ui.phone.DetailViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Info
import com.example.ui.components.*
import androidx.compose.ui.text.style.TextAlign

@Composable
fun TvDetailScreen(
    navController: NavController,
    mediaId: Int,
    mediaType: String,
    viewModel: DetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val detail = viewModel.detail
    val episodes = viewModel.episodes
    val trailers = viewModel.trailers
    val isSaved = viewModel.isSaved
    val anilistData = viewModel.anilistData
    val progressMap = viewModel.progressMap
    val watchedMap = viewModel.watchedMap
    val isLoading = viewModel.isLoading

    val backFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(mediaId, mediaType) {
        viewModel.load(mediaId, mediaType)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        if (isLoading || detail == null) {
            CircularProgressIndicator(
                color = Color(0xFFE50914),
                modifier = Modifier.align(Alignment.Center)
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
                                            Color(0xFF0A0A0A).copy(alpha = 0.6f),
                                            Color(0xFF0A0A0A)
                                        )
                                    )
                                )
                        )

                        // Back Button
                        var isBackFocused by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .focusRequester(backFocusRequester)
                                .onFocusChanged { isBackFocused = it.hasFocus }
                                .focusable()
                                .background(
                                    if (isBackFocused) Color(0xFFE50914) else Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }

                // 2. Poster, Title and metadata
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = (-40).dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AsyncImage(
                            model = TmdbApi.imgUrl(detail.posterPath, "w342"),
                            contentDescription = detail.displayTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = detail.displayTitle,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("★ %.1f".format(detail.voteAverage), color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(detail.displayYear, color = Color(0xFFCCCCCC), fontSize = 13.sp)
                                val durationText = if (detail is com.example.data.model.MovieDetail) {
                                    "${detail.runtime} min"
                                } else if (detail is com.example.data.model.TvDetail) {
                                    "${detail.numberOfSeasons} Seasons"
                                } else ""
                                if (durationText.isNotEmpty()) {
                                    Text(durationText, color = Color(0xFFCCCCCC), fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Genres
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                detail.genres.take(3).forEach { genre ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF222222), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(genre.name, color = Color(0xFFCCCCCC), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. CTA Action buttons row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = (-20).dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play action
                        TvActionButton(
                            label = "Play",
                            icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White) },
                            isPrimary = true,
                            onClick = {
                                coroutineScope.launch {
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
                            }
                        )

                        // Save action
                        TvActionButton(
                            label = if (isSaved) "Bookmarked" else "Bookmark",
                            icon = {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isSaved) Color(0xFFE50914) else Color.White
                                )
                            },
                            isPrimary = false,
                            onClick = { viewModel.toggleSave() }
                        )

                        // Trailer action
                        if (trailers.isNotEmpty()) {
                            TvActionButton(
                                label = "Trailer",
                                icon = { Icon(Icons.Default.Tv, contentDescription = "Trailer", tint = Color.White) },
                                isPrimary = false,
                                onClick = {
                                    val trailerKey = trailers.first().key
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$trailerKey"))
                                    context.startActivity(webIntent)
                                }
                            )
                        }
                    }
                }

                // Unreleased warning banner
                val releaseDate = when (detail) {
                    is MovieDetail -> (detail as MovieDetail).releaseDate
                    is TvDetail -> (detail as TvDetail).firstAirDate
                    else -> null
                }
                val isNotYetReleased = releaseDate?.let { dateStr ->
                    if (dateStr.isEmpty()) false else {
                        try {
                            val now = System.currentTimeMillis()
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            val date = sdf.parse(dateStr)
                            date != null && date.time > now
                        } catch (e: Exception) {
                            false
                        }
                    }
                } ?: false

                if (isNotYetReleased) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .offset(y = (-8).dp)
                                .background(Color(0xFFE50914).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE50914).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Unreleased",
                                    tint = Color(0xFFE50914),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Not Yet Released",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "This content has not been officially released yet. Streaming links may be unavailable or invalid.",
                                        color = Color(0xFFCCCCCC),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Overview
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = (-4).dp)
                    ) {
                        Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = detail.overview.ifEmpty { "No overview available." },
                            fontSize = 14.sp,
                            color = Color(0xFFCCCCCC),
                            lineHeight = 22.sp
                        )
                    }
                }

                // 5. AniList Integration (Anime only)
                if (anilistData != null) {
                    item {
                        val cleanedDesc = AniListApi.cleanDescription(anilistData.description)
                        if (cleanedDesc.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text("Anime Info (AniList)", fontSize = 18.sp, color = Color(0xFFE50914), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = cleanedDesc,
                                    fontSize = 14.sp,
                                    color = Color(0xFFCCCCCC),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // 6. Episodes (Series only)
                if ((mediaType == "tv" || mediaType == "anilist") && episodes.isNotEmpty()) {
                    if (mediaType == "tv" && detail is TvDetail && (detail as TvDetail).seasons.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "Seasons",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items((detail as TvDetail).seasons.filter { it.seasonNumber > 0 || it.episodeCount > 0 }, key = { it.seasonNumber }) { season ->
                                        var chipFocused by remember { mutableStateOf(false) }
                                        val isSelected = viewModel.selectedSeason == season.seasonNumber
                                        val chipBg = when {
                                            chipFocused -> Color.White
                                            isSelected -> Color(0xFFE50914)
                                            else -> Color(0xFF222222)
                                        }
                                        val chipTextColor = when {
                                            chipFocused -> Color.Black
                                            else -> Color.White
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(chipBg)
                                                .onFocusChanged { chipFocused = it.hasFocus }
                                                .focusable()
                                                .clickable {
                                                    viewModel.selectSeason(mediaId, season.seasonNumber)
                                                }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = season.name.ifEmpty { "Season ${season.seasonNumber}" },
                                                color = chipTextColor,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Episodes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    items(episodes, key = { "ep_${it.seasonNumber}_${it.episodeNumber}" }) { episode ->
                        val key = "tv_${mediaId}_s${episode.seasonNumber}_e${episode.episodeNumber}"
                        val isFullyWatched = watchedMap[key] ?: false

                        TvEpisodeRow(
                            episode = episode,
                            isWatched = isFullyWatched,
                            onClick = {
                                coroutineScope.launch {
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
                    }
                }

                // Cast (Credits) Section
                val castList = when (detail) {
                    is MovieDetail -> (detail as MovieDetail).credits?.cast
                    is TvDetail -> (detail as TvDetail).credits?.cast
                    else -> null
                }?.take(12)

                if (!castList.isNullOrEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                "Cast & Crew",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(castList, key = { it.id }) { cast ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(90.dp)
                                    ) {
                                        AsyncImage(
                                            model = TmdbApi.imgUrl(cast.profilePath, "w185"),
                                            contentDescription = cast.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF222222))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = cast.name,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            text = cast.character,
                                            fontSize = 10.sp,
                                            color = Color(0xFF999999),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Reviews Section
                val reviewsList = when (detail) {
                    is MovieDetail -> (detail as MovieDetail).reviews?.results
                    is TvDetail -> (detail as TvDetail).reviews?.results
                    else -> null
                }?.take(3)

                if (!reviewsList.isNullOrEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "User Reviews",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            reviewsList.forEach { review ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color(0xFFE50914).copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = review.author.take(1).uppercase(),
                                                color = Color(0xFFE50914),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Text(
                                            text = review.author,
                                            fontSize = 14.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = review.content,
                                        fontSize = 13.sp,
                                        color = Color(0xFFCCCCCC),
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Recommendations Section
                val recs = viewModel.recommendations
                if (recs.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                "More Like This",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(recs, key = { item -> item.id }) { item ->
                                    var recFocused by remember { mutableStateOf(false) }
                                    val borderStroke = if (recFocused) 2.dp else 0.dp
                                    val borderColor = if (recFocused) Color(0xFFE50914) else Color.Transparent

                                    Column(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .onFocusChanged { recFocused = it.hasFocus }
                                            .focusable()
                                            .clickable {
                                                navController.navigate(Routes.detail(item.id, item.mediaType))
                                            }
                                            .padding(4.dp)
                                    ) {
                                        AsyncImage(
                                            model = TmdbApi.imgUrl(item.posterPath, "w342"),
                                            contentDescription = item.displayTitle,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(120.dp, 180.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF222222))
                                                .border(borderStroke, borderColor, RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.displayTitle,
                                            fontSize = 13.sp,
                                            color = if (recFocused) Color(0xFFE50914) else Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun TvActionButton(
    label: String,
    icon: @Composable () -> Unit,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .onFocusChanged { focused = it.hasFocus }
            .focusable()
            .clickable(onClick = onClick),
        color = when {
            focused -> Color.White
            isPrimary -> Color(0xFFE50914)
            else -> Color(0xFF222222)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Apply proper color tint to icon depending on focus
            CompositionLocalProvider(
                LocalContentColor provides if (focused) Color.Black else Color.White
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (focused) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TvEpisodeRow(
    episode: TvEpisode,
    isWatched: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (focused) Color(0xFF1E1E1E) else Color.Transparent)
            .border(
                width = if (focused) 1.dp else 0.dp,
                color = if (focused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .onFocusChanged { focused = it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "S${episode.seasonNumber}E${episode.episodeNumber}",
            color = Color(0xFFE50914),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = episode.name.ifEmpty { "Episode ${episode.episodeNumber}" },
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (isWatched) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFE50914).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("WATCHED", color = Color(0xFFE50914), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
