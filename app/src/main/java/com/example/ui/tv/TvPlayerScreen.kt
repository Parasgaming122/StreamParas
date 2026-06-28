package com.example.ui.tv

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TvPlayerScreen(
    navController: NavController,
    streamUrl: String,
    mediaType: String,
    tmdbId: Int,
    season: Int? = null,
    episode: Int? = null,
    title: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showOverlay by remember { mutableStateOf(true) }

    // Auto-save progress key
    val progressKey = if (mediaType == "tv") {
        "tv_${tmdbId}_s${season ?: 1}_e${episode ?: 1}"
    } else {
        "movie_$tmdbId"
    }

    // Save progress on exit
    fun saveProgressAndExit() {
        // Since TV mode uses WebView, we mark as watched / saved to history directly on exit as progress tracking is handled through embedded iframe
        coroutineScope.launch {
            try {
                MediaRepository.markWatched(context, progressKey)
                val entry = com.example.data.model.HistoryEntry(
                    id = tmdbId,
                    title = title,
                    mediaType = mediaType,
                    season = season,
                    episode = episode,
                    watchedAt = System.currentTimeMillis()
                )
                MediaRepository.addHistoryEntry(context, entry)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                navController.popBackStack()
            }
        }
    }

    BackHandler {
        saveProgressAndExit()
    }

    // Overlay auto-hide delay loop
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000L)
            showOverlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { keyEvent ->
                // Intercept D-pad keys to control overlay
                val nativeEvent = keyEvent.nativeKeyEvent
                if (nativeEvent.action == KeyEvent.ACTION_DOWN) {
                    when (nativeEvent.keyCode) {
                        KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                            saveProgressAndExit()
                            return@onPreviewKeyEvent true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            showOverlay = !showOverlay
                            return@onPreviewKeyEvent true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                            // Turn overlay on when remote buttons are pushed
                            showOverlay = true
                            return@onPreviewKeyEvent false
                        }
                    }
                }
                false
            }
    ) {
        // Fullscreen WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    loadUrl(streamUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Leanback Control Overlay
        if (showOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // Top header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var backFocused by remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (backFocused) Color(0xFFE50914) else Color.White.copy(alpha = 0.1f))
                            .border(
                                width = if (backFocused) 2.dp else 0.dp,
                                color = if (backFocused) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .onFocusChanged { backFocused = it.isFocused }
                            .focusable()
                            .clickable { saveProgressAndExit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Centered D-pad virtual controllers
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(42.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var rewindFocused by remember { mutableStateOf(false) }
                    var playFocused by remember { mutableStateOf(false) }
                    var forwardFocused by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (rewindFocused) Color(0xFFE50914) else Color.White.copy(alpha = 0.1f))
                            .border(
                                width = if (rewindFocused) 2.dp else 0.dp,
                                color = if (rewindFocused) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .onFocusChanged { rewindFocused = it.isFocused }
                            .focusable()
                            .clickable {
                                // Info / Feedback Toast
                                android.widget.Toast.makeText(context, "Rewind (10s)", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Replay10,
                            contentDescription = "Rewind",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (playFocused) Color(0xFFE50914) else Color.White.copy(alpha = 0.2f))
                            .border(
                                width = if (playFocused) 2.dp else 0.dp,
                                color = if (playFocused) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .onFocusChanged { playFocused = it.isFocused }
                            .focusable()
                            .clickable {
                                showOverlay = !showOverlay
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (forwardFocused) Color(0xFFE50914) else Color.White.copy(alpha = 0.1f))
                            .border(
                                width = if (forwardFocused) 2.dp else 0.dp,
                                color = if (forwardFocused) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .onFocusChanged { forwardFocused = it.isFocused }
                            .focusable()
                            .clickable {
                                // Info / Feedback Toast
                                android.widget.Toast.makeText(context, "Forward (10s)", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Forward10,
                            contentDescription = "Forward",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Hint
                Text(
                    text = "Press D-PAD CENTER to toggle controls  ·  BACK to exit streaming",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}
