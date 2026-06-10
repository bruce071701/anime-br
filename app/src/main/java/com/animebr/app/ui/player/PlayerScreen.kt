package com.animebr.app.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn as AndroidOptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@AndroidOptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val videoUrl by viewModel.videoUrl.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val animeName by viewModel.animeName.collectAsStateWithLifecycle()
    val episodeTitle by viewModel.episodeTitle.collectAsStateWithLifecycle()
    val resumePosition by viewModel.resumePosition.collectAsStateWithLifecycle()
    val episodes by viewModel.episodes.collectAsStateWithLifecycle()
    val currentEpisodeIndex by viewModel.currentEpisodeIndex.collectAsStateWithLifecycle()
    val showEpisodeSelector by viewModel.showEpisodeSelector.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        if (showEpisodeSelector) {
            viewModel.toggleEpisodeSelector()
        } else {
            onBackClick()
        }
    }

    // Force landscape
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Carregando vídeo...", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
                }
            }
            videoUrl != null && videoUrl!!.isNotEmpty() -> {
                ExoPlayerView(
                    videoUrl = videoUrl!!,
                    resumePosition = resumePosition,
                    animeName = animeName,
                    episodeTitle = episodeTitle,
                    hasPrevious = viewModel.hasPreviousEpisode(),
                    hasNext = viewModel.hasNextEpisode(),
                    onBackClick = onBackClick,
                    onPrevious = { viewModel.playPreviousEpisode() },
                    onNext = { viewModel.playNextEpisode() },
                    onEpisodeList = { viewModel.toggleEpisodeSelector() },
                    onSaveProgress = { progress, duration -> viewModel.saveProgress(progress, duration) },
                    onRetryNextSource = { viewModel.tryNextSource() },
                    videoUrlForCast = videoUrl ?: ""
                )
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Não foi possível carregar o vídeo", color = Color.White, fontSize = 16.sp)
                    Text("Tente outro servidor", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        // Episode selector panel (slides in from right)
        AnimatedVisibility(
            visible = showEpisodeSelector,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            EpisodeSelectorPanel(
                episodes = episodes,
                currentIndex = currentEpisodeIndex,
                onEpisodeClick = { episode -> viewModel.switchEpisode(episode) },
                onDismiss = { viewModel.toggleEpisodeSelector() }
            )
        }
    }
}

@Composable
private fun EpisodeSelectorPanel(
    episodes: List<com.animebr.app.data.model.Episode>,
    currentIndex: Int,
    onEpisodeClick: (com.animebr.app.data.model.Episode) -> Unit,
    onDismiss: () -> Unit
) {
    val pageSize = 50
    val totalPages = if (episodes.size <= pageSize) 0 else (episodes.size + pageSize - 1) / pageSize
    var selectedPage by remember { mutableStateOf(if (currentIndex >= 0) currentIndex / pageSize else 0) }

    val displayEpisodes = if (totalPages == 0) episodes
    else {
        val start = selectedPage * pageSize
        val end = minOf(start + pageSize, episodes.size)
        episodes.subList(start, end)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Tap left area to dismiss
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )
        // Episode list panel
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(Color(0xE6000000))
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Episódios",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Page tabs
            if (totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (page in 0 until totalPages) {
                        val startIdx = page * pageSize
                        val endIdx = minOf(startIdx + pageSize - 1, episodes.size - 1)
                        val firstNum = episodes[startIdx].nums?.toIntOrNull() ?: (startIdx + 1)
                        val lastNum = episodes[endIdx].nums?.toIntOrNull() ?: (endIdx + 1)
                        val lo = minOf(firstNum, lastNum)
                        val hi = maxOf(firstNum, lastNum)
                        val isSelected = page == selectedPage
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFFFF6B35) else Color(0xFF333333))
                                .clickable { selectedPage = page }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("EP $lo-$hi", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(displayEpisodes) { index, episode ->
                    val actualIndex = selectedPage * pageSize + index
                    val isCurrentEp = actualIndex == currentIndex
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEpisodeClick(episode) }
                            .background(if (isCurrentEp) Color(0xFF333333) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Episódio ${episode.nums ?: "?"}",
                            color = if (isCurrentEp) Color(0xFFFF6B35) else Color.White,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@AndroidOptIn(UnstableApi::class)
@Composable
private fun ExoPlayerView(
    videoUrl: String,
    resumePosition: Long,
    animeName: String,
    episodeTitle: String,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onBackClick: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onEpisodeList: () -> Unit,
    onSaveProgress: (Long, Long) -> Unit,
    onRetryNextSource: () -> Unit,
    videoUrlForCast: String = ""
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) { delay(3000); showControls = false }
    }

    val exoPlayer = remember(videoUrl) {
        val isGoogleVideo = videoUrl.contains("googlevideo.com")
        val dataSourceFactory = if (isGoogleVideo) {
            val headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36",
                "Referer" to videoUrl,
                "Accept" to "*/*",
                "Accept-Language" to "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
                "Sec-Fetch-Dest" to "video",
                "Sec-Fetch-Mode" to "no-cors",
                "Sec-Fetch-Site" to "same-origin",
                "sec-ch-ua" to "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
                "sec-ch-ua-mobile" to "?0",
                "sec-ch-ua-platform" to "\"macOS\"",
                "x-browser-channel" to "stable",
                "x-browser-copyright" to "Copyright 2026 Google LLC. All Rights Reserved.",
                "x-browser-validation" to "z5FJMLtwNd1Yt40OJgdaJ8rqye0=",
                "x-browser-year" to "2026",
                "x-client-data" to "CKmdygEIlKHLAQiGoM0BCMa/zwEIzMeUMAjsyZQwCP3KlDAIrcuUMAjxy5QwCM3MlDAI0MyUMAjgzJQwCOLMlDAI7cyUMAj/zJQw"
            )
            androidx.media3.datasource.DefaultHttpDataSource.Factory()
                .setUserAgent(headers["User-Agent"])
                .setDefaultRequestProperties(headers)
        } else {
            androidx.media3.datasource.DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36")
        }
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        hasError = true
                        errorMessage = when (error.errorCode) {
                            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Erro de conexão"
                            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Tempo esgotado"
                            androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "Servidor indisponível"
                            else -> "Erro ao reproduzir"
                        }
                        // Auto-retry with next source
                        onRetryNextSource()
                    }
                })
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                if (resumePosition > 0) seekTo(resumePosition)
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
                isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
            }
            delay(500)
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            onSaveProgress(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0))
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showControls = !showControls }
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false } },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering indicator
        if (isBuffering && !hasError) {
            CircularProgressIndicator(
                color = Color(0xFFFF6B35),
                modifier = Modifier.align(Alignment.Center).size(48.dp)
            )
        }

        // Error overlay
        if (hasError) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Text(
                    text = "Tente outro servidor",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
                // Top bar: back + title + episode list button
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopStart).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(episodeTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(animeName, color = Color.Gray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    // Cast button
                    if (videoUrlForCast.isNotEmpty()) {
                        AndroidView(
                            factory = { ctx ->
                                val themedCtx = android.view.ContextThemeWrapper(ctx, androidx.appcompat.R.style.Theme_AppCompat_NoActionBar)
                                androidx.mediarouter.app.MediaRouteButton(themedCtx).apply {
                                    val selector = androidx.mediarouter.media.MediaRouteSelector.Builder()
                                        .addControlCategory(com.google.android.gms.cast.CastMediaControlIntent.categoryForCast(
                                            com.google.android.gms.cast.CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
                                        ))
                                        .build()
                                    routeSelector = selector
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    // Episode list button
                    IconButton(onClick = onEpisodeList) {
                        Icon(Icons.AutoMirrored.Filled.List, "Episodes", tint = Color.White)
                    }
                }

                // Center: prev + rewind + play/pause + forward + next
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous episode
                    IconButton(
                        onClick = { onPrevious() },
                        enabled = hasPrevious,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, "Previous", tint = if (hasPrevious) Color.White else Color.Gray, modifier = Modifier.size(30.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Rewind 10s
                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0)); showControls = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.FastRewind, "Rewind", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Play/Pause
                    IconButton(onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play(); showControls = true }, modifier = Modifier.size(64.dp)) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = Color.White, modifier = Modifier.size(48.dp))
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Forward 10s
                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)); showControls = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.FastForward, "Forward", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Next episode
                    IconButton(
                        onClick = { onNext() },
                        enabled = hasNext,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, "Next", tint = if (hasNext) Color.White else Color.Gray, modifier = Modifier.size(30.dp))
                    }
                }

                // Bottom: progress bar + time
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Slider(
                        value = if (isSeeking) seekPosition else { if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f },
                        onValueChange = { isSeeking = true; seekPosition = it },
                        onValueChangeFinished = { exoPlayer.seekTo((seekPosition * duration).toLong()); isSeeking = false; showControls = true },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF6B35), activeTrackColor = Color(0xFFFF6B35), inactiveTrackColor = Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(currentPosition), color = Color.White, fontSize = 12.sp)
                        Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
