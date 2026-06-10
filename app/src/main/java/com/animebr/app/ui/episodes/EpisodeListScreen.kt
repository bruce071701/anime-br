package com.animebr.app.ui.episodes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.animebr.app.data.model.Episode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeListScreen(
    onBackClick: () -> Unit,
    onEpisodeClick: (Int, Int) -> Unit,
    viewModel: EpisodeListViewModel = hiltViewModel()
) {
    val anime by viewModel.anime.collectAsStateWithLifecycle()
    val episodes by viewModel.episodes.collectAsStateWithLifecycle()
    val isAscending by viewModel.isAscending.collectAsStateWithLifecycle()
    val pageGroups by viewModel.pageGroups.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Episódios", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        anime?.let {
                            Text(it.name ?: "", color = Color(0xFFB0B0B0), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                },
                navigationIcon = {
                    com.animebr.app.ui.common.AppBackButton(onClick = onBackClick)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleSortOrder() },
                containerColor = Color(0xFFFF6B35),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (isAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isAscending) "Ascending" else "Descending"
                )
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Page tabs (only shown if more than PAGE_SIZE episodes)
            if (pageGroups.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pageGroups.forEachIndexed { index, label ->
                        val isSelected = index == currentPage
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFF6B35) else Color(0xFF2A2A2A))
                                .clickable { viewModel.selectPage(index) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Episode list
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                items(episodes) { episode ->
                    EpisodeListItem(
                        episode = episode,
                        onClick = { onEpisodeClick(viewModel.animeId, episode.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun EpisodeListItem(
    episode: Episode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                SubcomposeAsyncImage(
                    model = episode.imagen,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    loading = { Box(Modifier.fillMaxSize().background(Color(0xFF2A2A2A))) },
                    error = { Box(Modifier.fillMaxSize().background(Color(0xFF2A2A2A))) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = episode.title ?: "Episódio ${episode.nums}",
                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                val displayDate = episode.aired?.takeIf { it.isNotBlank() } ?: episode.createdAt?.take(10)
                if (!displayDate.isNullOrBlank()) {
                    Text(text = displayDate, color = Color(0xFFB0B0B0), fontSize = 12.sp)
                }
            }
        }
    }
}
