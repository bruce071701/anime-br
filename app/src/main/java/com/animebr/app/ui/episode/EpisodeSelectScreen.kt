package com.animebr.app.ui.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.animebr.app.data.model.Player
import com.animebr.app.ui.rating.RatingDialog
import com.animebr.app.ui.rating.RatingManager
import com.animebr.app.util.RegionChecker

@Composable
fun EpisodeSelectScreen(
    onBackClick: () -> Unit,
    onSourceClick: (Int, Int) -> Unit,
    onGoToDownloads: () -> Unit = {},
    ratingManager: RatingManager,
    viewModel: EpisodeSelectViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val anime by viewModel.anime.collectAsStateWithLifecycle()
    val episode by viewModel.episode.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val showDownloadToast by viewModel.showDownloadToast.collectAsStateWithLifecycle()

    // Region check: if not in Brazil, redirect to YouTube directly
    LaunchedEffect(anime) {
        val animeName = anime?.name
        if (animeName != null && !RegionChecker.isBrazilianUser(context)) {
            RegionChecker.openYouTubeSearch(context, animeName)
            onBackClick()
        }
    }

    // Show snackbar when download starts
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    LaunchedEffect(showDownloadToast) {
        if (showDownloadToast) {
            val result = snackbarHostState.showSnackbar(
                message = "Download iniciado",
                actionLabel = "Ver downloads",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onGoToDownloads()
            }
            viewModel.dismissDownloadToast()
        }
    }

    // Track if we should show rating dialog (set to true when returning from player)
    var showRatingDialog by rememberSaveable { mutableStateOf(false) }
    var hasNavigatedToPlayer by rememberSaveable { mutableStateOf(false) }

    // Show rating dialog when returning from player
    val shouldShowRating = showRatingDialog && ratingManager.shouldShowRating()

    if (shouldShowRating) {
        RatingDialog(
            onDismiss = {
                showRatingDialog = false
            },
            onRatingSubmitted = { rating ->
                ratingManager.onRatingSubmitted()
                showRatingDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            com.animebr.app.ui.common.AppBackButton(onClick = onBackClick)
            Column {
                Text(
                    text = "Episódio ${episode?.nums ?: ""}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = anime?.name ?: "",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Disclaimer
        Text(
            text = "Atención: No almacenamos ningún video dentro de nuestros servidores, " +
                    "todos son enlaces externos y su disponibilidad no depende de nosotros. " +
                    "En caso de que tenga problemas para ver el video, envíenos sus comentarios. " +
                    "Gracias por tu comprensión.",
            color = Color(0xFFFF9800),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            error != null && players.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = error ?: "", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.retry() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Text("  Tentar novamente", color = Color.White)
                    }
                }
            }
            else -> {
                players.forEachIndexed { index, player ->
                    PlayerItem(
                        player = player,
                        index = index + 1,
                        onClick = {
                            hasNavigatedToPlayer = true
                            // When user comes back from player, show rating
                            showRatingDialog = true
                            ratingManager.onRatingShown()
                            onSourceClick(viewModel.getEpisodeId(), player.id)
                        },
                        onDownload = {
                            viewModel.downloadEpisode(player)
                        }
                    )
                    HorizontalDivider(color = Color(0xFF333333))
                }
            }
        }
        }

        // Snackbar
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)
        )
    }
}

@Composable
fun PlayerItem(
    player: Player,
    index: Int,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Assistir - ${player.server ?: "Server $index"}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        )
        IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Download,
                contentDescription = "Download",
                tint = Color(0xFFFF6B35),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
