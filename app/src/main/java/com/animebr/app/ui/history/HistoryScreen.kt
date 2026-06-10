package com.animebr.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar histórico", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja limpar todo o histórico de reprodução?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllHistory()
                    showClearDialog = false
                }) {
                    Text("Confirmar", color = Color(0xFFFF6B35))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar", color = Color(0xFFB0B0B0))
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0B0B0),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    com.animebr.app.ui.common.AppBackButton(onClick = onBackClick)
                },
                actions = {
                    if (historyItems.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Delete, "Clear", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF000000),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF000000)
    ) { padding ->
        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum histórico de reprodução",
                    color = Color(0xFFB0B0B0),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(historyItems) { item ->
                    HistoryListItem(
                        item = item,
                        onClick = { item.anime?.let { onAnimeClick(it.id) } }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryListItem(
    item: HistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail 16:9 with progress bar overlay
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                SubcomposeAsyncImage(
                    model = item.anime?.imagen,
                    contentDescription = item.anime?.name,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2A2A2A))
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Dark overlay at bottom for progress bar visibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Color.Black.copy(alpha = 0.5f)
                        )
                )

                // Progress bar at bottom
                if (item.history.duration > 0) {
                    val progress = (item.history.progress.toFloat() / item.history.duration.toFloat())
                        .coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = Color(0xFFFF6B35),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = item.anime?.name ?: "Unknown",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Episódio ${item.history.episodeNumber}",
                    color = Color(0xFFB0B0B0),
                    fontSize = 13.sp
                )
                // Show progress percentage
                if (item.history.duration > 0) {
                    val progressPercent = ((item.history.progress.toFloat() / item.history.duration.toFloat()) * 100)
                        .coerceIn(0f, 100f).toInt()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$progressPercent% assistido",
                        color = Color(0xFFFF6B35),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
