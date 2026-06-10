package com.animebr.app.ui.downloads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.animebr.app.data.model.Download

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBackClick: () -> Unit,
    onPlayClick: (String) -> Unit, // filePath
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Limpar downloads", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja excluir todos os downloads?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAll(); showDeleteAllDialog = false }) {
                    Text("Confirmar", color = Color(0xFFFF6B35))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
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
                title = { Text("Downloads", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    com.animebr.app.ui.common.AppBackButton(onClick = onBackClick)
                },
                actions = {
                    if (downloads.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete all", tint = Color.White)
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
        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum download", color = Color(0xFFB0B0B0), fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(downloads) { download ->
                    DownloadItem(
                        download = download,
                        onPause = { viewModel.pauseDownload(download.id) },
                        onResume = { viewModel.resumeDownload(download.id) },
                        onDelete = { viewModel.deleteDownload(download.id) },
                        onPlay = { download.filePath?.let { onPlayClick(it) } }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun DownloadItem(
    download: Download,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.animeName ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = download.episodeTitle ?: "Episódio ${download.episodeNumber}",
                    color = Color(0xFFB0B0B0),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar + status
                when (download.status) {
                    Download.STATUS_DOWNLOADING, Download.STATUS_PENDING -> {
                        if (download.progress > 0f && download.fileSize > 0) {
                            LinearProgressIndicator(
                                progress = { download.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFFFF6B35),
                                trackColor = Color(0xFF333333)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFFFF6B35),
                                trackColor = Color(0xFF333333)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                download.status == Download.STATUS_PENDING -> "Aguardando..."
                                download.fileSize > 0 -> "${(download.progress * 100).toInt()}% • ${formatFileSize(download.downloadedSize)} / ${formatFileSize(download.fileSize)}"
                                download.downloadedSize > 0 -> "Baixando • ${formatFileSize(download.downloadedSize)}"
                                else -> "Iniciando..."
                            },
                            color = Color(0xFFB0B0B0),
                            fontSize = 11.sp
                        )
                    }
                    Download.STATUS_PAUSED -> {
                        LinearProgressIndicator(
                            progress = { download.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFFFC107),
                            trackColor = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pausado - ${(download.progress * 100).toInt()}%",
                            color = Color(0xFFFFC107),
                            fontSize = 11.sp
                        )
                    }
                    Download.STATUS_COMPLETED -> {
                        Text(
                            text = "Concluído - ${formatFileSize(download.fileSize)}",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Download.STATUS_FAILED -> {
                        Text(
                            text = "Falhou - toque para tentar novamente",
                            color = Color(0xFFEF5350),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action buttons
            when (download.status) {
                Download.STATUS_DOWNLOADING, Download.STATUS_PENDING -> {
                    IconButton(onClick = onPause, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Pause, "Pause", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Download.STATUS_PAUSED, Download.STATUS_FAILED -> {
                    IconButton(onClick = onResume, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PlayArrow, "Resume", tint = Color(0xFFFF6B35), modifier = Modifier.size(20.dp))
                    }
                }
                Download.STATUS_COMPLETED -> {
                    IconButton(onClick = onPlay, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, "Delete", tint = Color(0xFF808080), modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
