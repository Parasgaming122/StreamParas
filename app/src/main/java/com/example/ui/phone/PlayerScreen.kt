package com.example.ui.phone

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.data.repository.MediaRepository
import com.example.player.PlayerState
import com.example.player.VideoViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    navController: NavController,
    streamUrl: String,
    mediaType: String,
    tmdbId: Int,
    season: Int? = null,
    episode: Int? = null,
    title: String,
    viewModel: VideoViewModel = viewModel()
) {
    val context = LocalContext.current
    val isDirectStream = streamUrl.lowercase(Locale.US).contains(".m3u8")

    // Force landscape orientation during video playback
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) {
                break
            }
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    DisposableEffect(activity) {
        val originalOrientation = activity?.requestedOrientation ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    // Back button override
    BackHandler {
        navController.popBackStack()
    }

    DisposableEffect(streamUrl) {
        if (isDirectStream) {
            viewModel.loadStream(streamUrl)
        }
        onDispose {
            viewModel.player.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isDirectStream) {
            // Direct Stream HLS Player (ExoPlayer)
            ExoPlayerLayout(
                viewModel = viewModel,
                navController = navController,
                mediaType = mediaType,
                tmdbId = tmdbId,
                season = season,
                episode = episode,
                title = title
            )
        } else {
            // WebView Embed Iframe Player
            WebViewLayout(url = streamUrl, navController = navController)
        }
    }
}

@Composable
fun ExoPlayerLayout(
    viewModel: VideoViewModel,
    navController: NavController,
    mediaType: String,
    tmdbId: Int,
    season: Int?,
    episode: Int?,
    title: String
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val progressPercent by viewModel.progressPercent.collectAsState()
    val showControls by viewModel.showControls.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Auto-hide controls timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000L)
            viewModel.hideControls()
        }
    }

    // Auto-save watch progress loop
    val progressKey = if (mediaType == "tv") {
        "tv_${tmdbId}_s${season ?: 1}_e${episode ?: 1}"
    } else {
        "movie_$tmdbId"
    }

    LaunchedEffect(playerState) {
        if (playerState == PlayerState.READY) {
            while (true) {
                delay(5000L)
                val (current, duration) = viewModel.getProgressInfo()
                if (duration > 0) {
                    val progressRatio = current.toFloat() / duration
                    MediaRepository.saveProgress(context, progressKey, progressRatio)

                    // Auto-mark watched check if remaining duration <= 20 seconds
                    val remainingSecs = (duration - current) / 1000L
                    if (remainingSecs <= 20L) {
                        MediaRepository.markWatched(context, progressKey)
                        // Add history entry
                        val entry = com.example.data.model.HistoryEntry(
                            id = tmdbId,
                            title = title,
                            mediaType = mediaType,
                            season = season,
                            episode = episode,
                            watchedAt = System.currentTimeMillis()
                        )
                        MediaRepository.addHistoryEntry(context, entry)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        // Left half seeks back, right half seeks forward
                        val width = size.width
                        if (offset.x < width / 2) {
                            viewModel.seekBackward()
                        } else {
                            viewModel.seekForward()
                        }
                    }
                )
            }
    ) {
        // AndroidView wrapping PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.player
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading/Buffering overlay indicator
        if (playerState == PlayerState.BUFFERING) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(54.dp)
            )
        }

        // Custom Overlay Controls
        if (showControls) {
            // Dark vignette backplates
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Top control header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1
                )
            }

            // Central control trigger buttons (Replay10, Play/Pause, Forward10)
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.seekBackward() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.Replay10,
                        contentDescription = "Backward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                        .clickable { viewModel.togglePlayPause() }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(42.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.seekForward() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.Forward10,
                        contentDescription = "Forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Bottom controls seek bar panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (current, duration) = viewModel.getProgressInfo()
                    Text(
                        text = formatTime(current),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = formatTime(duration),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = progressPercent,
                    onValueChange = { percent ->
                        viewModel.seekToPosition(percent)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Red,
                        inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_seek_slider")
                )
            }
        }
    }
}

@Composable
fun WebViewLayout(
    url: String,
    navController: NavController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close Floating Button for embed views
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Exit Stream",
                tint = Color.White
            )
        }
    }
}

// Convert timestamp millisecond values to clean HH:MM:SS format
private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000L
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
