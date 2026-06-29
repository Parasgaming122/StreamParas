package com.example.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.phone.SearchViewModel
import kotlinx.coroutines.delay

@Composable
fun TvSearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val query = viewModel.query
    val results = viewModel.results
    val isLoading = viewModel.isLoading

    val searchFocusRequester = remember { FocusRequester() }

    // Auto focus search box on load
    LaunchedEffect(Unit) {
        delay(300L)
        searchFocusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // 1. Sidebar Nav
        TvSidebar(navController = navController, activeRoute = Routes.SEARCH)

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(24.dp)
        ) {
            Text(
                "Search",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text search box
            var isBoxFocused by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text("Search title, series, anime...", color = Color(0xFF666666)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF888888)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF888888))
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF161616),
                    unfocusedContainerColor = Color(0xFF111111),
                    focusedBorderColor = Color(0xFFE50914),
                    unfocusedBorderColor = Color(0xFF333333)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocusRequester)
                    .onFocusChanged { isBoxFocused = it.hasFocus }
                    .border(
                        width = if (isBoxFocused) 2.dp else 0.dp,
                        color = if (isBoxFocused) Color(0xFFE50914) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFFE50914),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (query.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Type your keywords to start searching",
                            color = Color(0xFF888888),
                            fontSize = 14.sp
                        )
                    }
                } else if (results.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No results found for \"$query\"",
                            color = Color(0xFF888888),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(results) { item ->
                            TvSearchResultRow(
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
}

@Composable
fun TvSearchResultRow(
    item: MediaItem,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val posterUrl = TmdbApi.imgUrl(item.posterPath, "w92")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (focused) Color(0xFF1A1A1A) else Color.Transparent)
            .border(
                width = if (focused) 1.dp else 0.dp,
                color = if (focused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .onFocusChanged { focused = it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = posterUrl,
            contentDescription = item.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp, 72.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = item.displayTitle,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(item.displayYear, color = Color(0xFF888888), fontSize = 12.sp)
                Text("★ %.1f".format(item.voteAverage), color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(if (item.mediaType == "anilist") "AniList" else if (item.isTv) "Series" else "Movie", color = Color(0xFFE50914), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
