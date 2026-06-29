package com.example.ui.phone

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.api.TmdbApi
import com.example.data.local.Prefs
import com.example.data.model.HistoryEntry
import com.example.data.model.SavedItem
import com.example.data.repository.MediaRepository
import com.example.ui.navigation.Routes
import com.example.ui.theme.LocalStreambertColors
import com.example.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val _continueWatching = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val continueWatching: StateFlow<List<HistoryEntry>> = _continueWatching

    private val _watchlist = MutableStateFlow<List<SavedItem>>(emptyList())
    val watchlist: StateFlow<List<SavedItem>> = _watchlist

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history

    fun loadLibraryData() {
        viewModelScope.launch {
            try {
                val fullHistory = Prefs.getWatchHistory(getApplication())
                val progressMap = Prefs.getWatchProgress(getApplication())
                val watchedMap = Prefs.getWatched(getApplication())
                
                // Get watchlist
                val savedMap = Prefs.getSaved(getApplication())
                val savedOrder = Prefs.getSavedOrder(getApplication())
                val orderedSaved = savedOrder.mapNotNull { savedMap[it] }
                _watchlist.value = orderedSaved

                // Split history into Continue Watching vs General History
                val contWatching = mutableListOf<HistoryEntry>()
                val generalHistory = mutableListOf<HistoryEntry>()

                fullHistory.forEach { entry ->
                    val key = if (entry.mediaType == "tv") {
                        "tv_${entry.id}_s${entry.season ?: 1}_e${entry.episode ?: 1}"
                    } else {
                        "movie_${entry.id}"
                    }
                    val progress = progressMap[key] ?: 0f
                    val isWatched = watchedMap[key] ?: false

                    if (progress in 0.02f..0.98f && !isWatched) {
                        contWatching.add(entry.copy(progress = progress))
                    } else {
                        generalHistory.add(entry.copy(progress = progress))
                    }
                }

                _continueWatching.value = contWatching
                _history.value = generalHistory
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Failed to load library data", e)
            }
        }
    }

    fun removeHistoryItem(entry: HistoryEntry) {
        viewModelScope.launch {
            val list = Prefs.getWatchHistory(getApplication()).toMutableList()
            list.removeAll { it.id == entry.id && it.mediaType == entry.mediaType && it.season == entry.season && it.episode == entry.episode }
            Prefs.setWatchHistory(getApplication(), list)
            loadLibraryData()
        }
    }
}

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = viewModel()
) {
    val colors = LocalStreambertColors.current
    val continueWatching by viewModel.continueWatching.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val history by viewModel.history.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLibraryData()
    }

    val isEmpty = continueWatching.isEmpty() && watchlist.isEmpty() && history.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        if (isEmpty) {
            // Empty state view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = "Empty",
                    tint = colors.text3,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nothing here yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.text2,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your bookmarked items, continue watching progress, and watch history will show up here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text3,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "My Library",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your curated bookmarks and watch trackings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.text2
                        )
                    }
                }

                // Section 1: Continue Watching
                if (continueWatching.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Continue Watching")
                    }
                    items(continueWatching, key = { "cw_${it.id}_${it.mediaType}_${it.season ?: 0}_${it.episode ?: 0}" }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onPlayClick = {
                                navController.navigate(Routes.detail(entry.id, entry.mediaType))
                            },
                            onDeleteClick = {
                                viewModel.removeHistoryItem(entry)
                            }
                        )
                    }
                }

                // Section 2: Watchlist
                if (watchlist.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Watchlist (${watchlist.size})")
                    }
                    items(watchlist, key = { "wl_${it.id}" }) { item ->
                        WatchlistRow(
                            item = item,
                            onClick = {
                                navController.navigate(Routes.detail(item.id, item.mediaType))
                            }
                        )
                    }
                }

                // Section 3: Watch History
                if (history.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Watch History")
                    }
                    items(history, key = { "h_${it.id}_${it.mediaType}_${it.season ?: 0}_${it.episode ?: 0}" }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onPlayClick = {
                                navController.navigate(Routes.detail(entry.id, entry.mediaType))
                            },
                            onDeleteClick = {
                                viewModel.removeHistoryItem(entry)
                            }
                        )
                    }
                }

                // Spacer at end of scroll
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    val colors = LocalStreambertColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.accent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        HorizontalDivider(color = colors.border, thickness = 1.dp)
    }
}

@Composable
fun HistoryRow(
    entry: HistoryEntry,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = LocalStreambertColors.current
    val posterUrl = TmdbApi.imgUrl(entry.posterPath, "w92")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress()
            .clickable(onClick = onPlayClick)
            .background(colors.surface, RoundedCornerShape(8.dp))
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = posterUrl,
            contentDescription = entry.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(42.dp, 62.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surface2)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            val metadata = if (entry.mediaType == "tv") {
                "S${entry.season} E${entry.episode} - ${entry.episodeName ?: "Episode"}"
            } else {
                "Movie"
            }

            Text(
                text = metadata,
                style = MaterialTheme.typography.labelSmall,
                color = colors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Progress bar if partial
            if (entry.progress in 0.02f..0.98f) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(3.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(entry.progress)
                            .background(colors.accent)
                    )
                }
            }
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = colors.text3,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun WatchlistRow(
    item: SavedItem,
    onClick: () -> Unit
) {
    val colors = LocalStreambertColors.current
    val posterUrl = TmdbApi.imgUrl(item.posterPath, "w92")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress()
            .clickable(onClick = onClick)
            .background(colors.surface, RoundedCornerShape(8.dp))
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = posterUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(42.dp, 62.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surface2)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.year,
                    color = colors.text2,
                    fontSize = 11.sp
                )

                if (item.voteAverage > 0f) {
                    Text(
                        text = "★ %.1f".format(item.voteAverage),
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(colors.surface3, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.mediaType == "tv") "Series" else "Movie",
                        color = colors.text2,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
