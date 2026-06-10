package com.animebr.app.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn as AndroidOptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.io.File

/**
 * Simple player for local downloaded files.
 */
@AndroidOptIn(UnstableApi::class)
@Composable
fun LocalPlayerScreen(
    filePath: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    BackHandler { onBackClick() }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) { delay(3000); showControls = false }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.fromFile(File(filePath))
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isSeeking) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0)
                duration = exoPlayer.duration.coerceAtLeast(0)
                isPlaying = exoPlayer.isPlaying
            }
            delay(500)
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showControls = !showControls }
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false } },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
                // Top: back button + file name
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopStart).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = File(filePath).nameWithoutExtension,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Center: controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0)) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.FastRewind, "Rewind", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    IconButton(onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() }, modifier = Modifier.size(64.dp)) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.FastForward, "Forward", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                // Bottom: progress
                Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Slider(
                        value = if (isSeeking) seekPosition else { if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f },
                        onValueChange = { isSeeking = true; seekPosition = it },
                        onValueChangeFinished = { exoPlayer.seekTo((seekPosition * duration).toLong()); isSeeking = false },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF6B35), activeTrackColor = Color(0xFFFF6B35), inactiveTrackColor = Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTimeLocal(currentPosition), color = Color.White, fontSize = 12.sp)
                        Text(formatTimeLocal(duration), color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun formatTimeLocal(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
