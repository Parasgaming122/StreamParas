package com.example.ui.phone

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.MediaItem
import com.example.data.repository.MediaRepository
import com.example.ui.navigation.Routes
import com.example.ui.theme.LocalStreambertColors
import com.example.ui.components.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShimmerSearchResultRow(shimmerBrush: androidx.compose.ui.graphics.Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp, 65.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    var query by mutableStateOf("")
    var results by mutableStateOf<List<MediaItem>>(emptyList())
    var searchHistory by mutableStateOf<List<String>>(emptyList())
    var isLoading by mutableStateOf(false)

    private var searchJob: Job? = null

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            searchHistory = Prefs.getSearchHistory(getApplication())
        }
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            results = emptyList()
            isLoading = false
            return
        }

        searchJob = viewModelScope.launch {
            isLoading = true
            delay(380) // 380ms debounce
            try {
                results = MediaRepository.search(newQuery)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed for query: $newQuery", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun executeSearch(searchQuery: String) {
        query = searchQuery
        onQueryChange(searchQuery)
        saveSearchQuery(searchQuery)
    }

    fun saveSearchQuery(q: String) {
        if (q.isBlank()) return
        viewModelScope.launch {
            val history = Prefs.getSearchHistory(getApplication()).toMutableList()
            history.remove(q)
            history.add(0, q)
            Prefs.setSearchHistory(getApplication(), history)
            searchHistory = history.take(12)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            Prefs.setSearchHistory(getApplication(), emptyList())
            searchHistory = emptyList()
        }
    }

    fun deleteHistoryItem(item: String) {
        viewModelScope.launch {
            val history = Prefs.getSearchHistory(getApplication()).toMutableList()
            history.remove(item)
            Prefs.setSearchHistory(getApplication(), history)
            searchHistory = history
        }
    }
}

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val colors = LocalStreambertColors.current
    val query = viewModel.query
    val results = viewModel.results
    val searchHistory = viewModel.searchHistory
    val isLoading = viewModel.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // Search text field
        OutlinedTextField(
            value = query,
            onValueChange = {
                viewModel.onQueryChange(it)
            },
            placeholder = { Text("Search movies, series, anime...", color = colors.text3) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.text2) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.text2)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                focusedContainerColor = colors.surface2,
                unfocusedContainerColor = colors.surface,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                cursorColor = colors.accent
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("search_input")
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) {
                val brush = shimmerBrush()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(6) {
                        ShimmerSearchResultRow(shimmerBrush = brush)
                    }
                }
            } else if (query.isBlank()) {
                // Render Recent Search History
                if (searchHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent Searches",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.text,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { viewModel.clearHistory() }) {
                                Text("Clear all", color = colors.accent)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(searchHistory, key = { it }) { historyTerm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .scaleOnPress()
                                        .clickable {
                                            viewModel.executeSearch(historyTerm)
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "History",
                                        tint = colors.text3,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = historyTerm,
                                        color = colors.text2,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteHistoryItem(historyTerm) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Delete",
                                            tint = colors.text3,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = colors.border)
                            }
                        }
                    }
                } else {
                    // Empty state when no history and no query
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.text3,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Find your next watch",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text2
                        )
                        Text(
                            text = "Search by movie title, series, or anime",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.text3
                        )
                    }
                }
            } else if (results.isEmpty()) {
                // No results empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No results found for \"$query\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.text2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching with different keywords",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text3
                    )
                }
            } else {
                // Results List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results, key = { it.id }) { item ->
                        SearchResultRow(
                            item = item,
                            onClick = {
                                viewModel.saveSearchQuery(item.displayTitle)
                                navController.navigate(Routes.detail(item.id, item.mediaType))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    item: MediaItem,
    onClick: () -> Unit
) {
    val colors = LocalStreambertColors.current
    val posterUrl = TmdbApi.imgUrl(item.posterPath, "w92")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster thumbnail
        AsyncImage(
            model = posterUrl,
            contentDescription = item.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(45.dp, 65.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surface2)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.displayTitle,
                style = MaterialTheme.typography.titleMedium,
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
                    text = item.displayYear,
                    color = colors.text2,
                    fontSize = 12.sp
                )

                if (item.voteAverage > 0f) {
                    Text(
                        text = "★ %.1f".format(item.voteAverage),
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(if (item.mediaType == "anilist") colors.accent.copy(alpha = 0.2f) else colors.surface3, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.mediaType == "anilist") "AniList" else if (item.isTv) "Series" else "Movie",
                        color = if (item.mediaType == "anilist") colors.accent else colors.text2,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
