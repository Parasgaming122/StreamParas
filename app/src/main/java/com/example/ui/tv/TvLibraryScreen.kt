package com.example.ui.tv

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.navigation.Routes
import com.example.ui.phone.LibraryViewModel
import com.example.ui.phone.SettingsViewModel

@Composable
fun TvLibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = viewModel()
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val history by viewModel.history.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLibraryData()
    }

    val isEmpty = watchlist.isEmpty() && history.isEmpty() && continueWatching.isEmpty()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // 1. Sidebar Nav
        TvSidebar(navController = navController, activeRoute = Routes.LIBRARY)

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(24.dp)
        ) {
            Text(
                "My Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEmpty) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = "Library",
                        tint = Color(0xFF444444),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Nothing here yet",
                        color = Color(0xFF888888),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Watchlist Section
                    if (watchlist.isNotEmpty()) {
                        item {
                            Text(
                                "Watchlist (${watchlist.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE50914)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(watchlist) { item ->
                            TvTextItemRow(
                                title = item.title,
                                label = "${item.year} · ★ ${"%.1f".format(item.voteAverage)}",
                                onClick = {
                                    navController.navigate(Routes.detail(item.id, item.mediaType))
                                }
                            )
                        }
                    }

                    // Watch History Section
                    val allHistory = continueWatching + history
                    if (allHistory.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Watch History & Continuation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE50914)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(allHistory) { entry ->
                            val suffix = if (entry.mediaType == "tv") {
                                "S${entry.season}E${entry.episode}"
                            } else {
                                "Movie"
                            }
                            TvTextItemRow(
                                title = entry.title,
                                label = suffix,
                                onClick = {
                                    navController.navigate(Routes.detail(entry.id, entry.mediaType))
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
fun TvSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // 1. Sidebar Nav
        TvSidebar(navController = navController, activeRoute = Routes.SETTINGS)

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "TV Leanback Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. TV Mode Row (Interactive Toggle!)
            TvSettingsClickableRow(
                label = "TV UI Mode Status",
                value = if (viewModel.isTvMode) "ENABLED (TV Mode)" else "DISABLED (Mobile Mode)",
                onClick = {
                    val targetMode = !viewModel.isTvMode
                    viewModel.setTvModeValue(targetMode)
                    android.widget.Toast.makeText(
                        context,
                        "TV Mode changed! Restart the app to apply.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            )

            // 2. App Theme Preset Row (Interactive Cycle!)
            val themes = listOf("dark", "amoled", "mocha", "slate", "light")
            TvSettingsClickableRow(
                label = "UI Theme Aesthetic Style",
                value = viewModel.theme.uppercase(),
                onClick = {
                    val currentIndex = themes.indexOf(viewModel.theme).coerceAtLeast(0)
                    val nextIndex = (currentIndex + 1) % themes.size
                    viewModel.setThemeValue(themes[nextIndex])
                }
            )

            // 3. Accent Color Row (Interactive Cycle!)
            val accentColors = listOf("#e50914", "#ffb300", "#1e88e5", "#43a047", "#8e24aa")
            val accentNames = mapOf(
                "#e50914" to "Red (Default)",
                "#ffb300" to "Gold Amber",
                "#1e88e5" to "Vibrant Blue",
                "#43a047" to "Emerald Green",
                "#8e24aa" to "Royal Purple"
            )
            TvSettingsClickableRow(
                label = "System Highlight Color Accent",
                value = accentNames[viewModel.accentColor] ?: viewModel.accentColor.uppercase(),
                onClick = {
                    val currentIndex = accentColors.indexOf(viewModel.accentColor).coerceAtLeast(0)
                    val nextIndex = (currentIndex + 1) % accentColors.size
                    viewModel.setAccentColorValue(accentColors[nextIndex])
                }
            )

            // 4. Default Playback Source Row (Interactive Cycle!)
            val sourcesList = listOf("vidking", "videasy", "videasy_to", "vidsrc", "allmanga")
            val sourceLabels = mapOf(
                "vidking" to "Vidking Stream",
                "videasy" to "Videasy (.net) Player",
                "videasy_to" to "Videasy (.to) Player",
                "vidsrc" to "VidSrc (HLS Native)",
                "allmanga" to "AllManga Embed"
            )
            TvSettingsClickableRow(
                label = "Video Player Playback Source",
                value = sourceLabels[viewModel.playerSource] ?: viewModel.playerSource.uppercase(),
                onClick = {
                    val currentIndex = sourcesList.indexOf(viewModel.playerSource).coerceAtLeast(0)
                    val nextIndex = (currentIndex + 1) % sourcesList.size
                    viewModel.setPlayerSourceValue(sourcesList[nextIndex])
                }
            )

            // 5. TMDB Access Token Display Row
            val hasToken = viewModel.tmdbKey.isNotBlank()
            val tokenDisplay = if (hasToken) "ACTIVE (...${viewModel.tmdbKey.takeLast(4)})" else "MISSING"
            TvSettingsDisplayRow(
                label = "TMDB API Read Token Status",
                value = tokenDisplay
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info note block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161616))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Streambert TV Leanback Mode is fully active.\n\nYou can click any settings card above to cycle through available configurations. Adjusting options updates your layout and video players instantly.",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun TvSettingsClickableRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (focused) Color(0xFF222222) else Color(0xFF111111))
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .onFocusChanged { focused = it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (focused) Color.White else Color(0xFF888888),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "◀ ▶",
                color = if (focused) Color(0xFFE50914) else Color(0xFF444444),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TvTextItemRow(
    title: String,
    label: String,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (focused) Color(0xFF222222) else Color(0xFF111111))
            .border(
                width = if (focused) 1.dp else 0.dp,
                color = if (focused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .onFocusChanged { focused = it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.0f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            color = Color(0xFFE50914),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TvSettingsDisplayRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF111111))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF888888), fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
