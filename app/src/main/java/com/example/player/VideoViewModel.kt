package com.example.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PlayerState { IDLE, BUFFERING, READY, ENDED }

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState

    private val _progressPercent = MutableStateFlow(0f)
    val progressPercent: StateFlow<Float> = _progressPercent

    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    var player: ExoPlayer = ExoPlayer.Builder(getApplication()).build().apply {
        playWhenReady = true
    }
        private set

    private var progressTrackingJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_IDLE -> _playerState.value = PlayerState.IDLE
                    Player.STATE_BUFFERING -> _playerState.value = PlayerState.BUFFERING
                    Player.STATE_READY -> _playerState.value = PlayerState.READY
                    Player.STATE_ENDED -> _playerState.value = PlayerState.ENDED
                }
            }

            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                _isPlaying.value = isPlayingChanged
                if (isPlayingChanged) {
                    startProgressTracking()
                } else {
                    stopProgressTracking()
                }
            }
        })
    }

    fun loadStream(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekForward() {
        val target = player.currentPosition + 10000L
        player.seekTo(target.coerceAtMost(player.duration))
    }

    fun seekBackward() {
        val target = player.currentPosition - 10000L
        player.seekTo(target.coerceAtLeast(0L))
    }

    fun toggleControls() {
        _showControls.value = !_showControls.value
    }

    fun hideControls() {
        _showControls.value = false
    }

    fun showControls() {
        _showControls.value = true
    }

    fun getProgressInfo(): Pair<Long, Long> {
        val duration = player.duration.coerceAtLeast(1L)
        val current = player.currentPosition
        return Pair(current, duration)
    }

    fun seekToPosition(percent: Float) {
        val duration = player.duration
        if (duration > 0) {
            val target = (duration * percent).toLong()
            player.seekTo(target)
        }
    }

    private fun startProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val duration = player.duration
                if (duration > 0) {
                    _progressPercent.value = player.currentPosition.toFloat() / duration
                }
            }
        }
    }

    private fun stopProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    override fun onCleared() {
        stopProgressTracking()
        player.release()
        super.onCleared()
    }
}
