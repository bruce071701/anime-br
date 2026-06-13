package com.animebr.app.ui.genres

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val genreGradients = listOf(
    listOf(Color(0xFFFF6B35), Color(0xFFFF8F65)),
    listOf(Color(0xFF6C63FF), Color(0xFF9B94FF)),
    listOf(Color(0xFF00BFA5), Color(0xFF64FFDA)),
    listOf(Color(0xFFE91E63), Color(0xFFFF5C8D)),
    listOf(Color(0xFF3F51B5), Color(0xFF7986CB)),
    listOf(Color(0xFFFF9800), Color(0xFFFFCC02)),
    listOf(Color(0xFF009688), Color(0xFF4DB6AC)),
    listOf(Color(0xFF8E24AA), Color(0xFFCE93D8)),
    listOf(Color(0xFF1976D2), Color(0xFF64B5F6)),
    listOf(Color(0xFFF44336), Color(0xFFFF7043)),
    listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
    listOf(Color(0xFF795548), Color(0xFFA1887F)),
    listOf(Color(0xFF607D8B), Color(0xFF90A4AE)),
    listOf(Color(0xFFFF5722), Color(0xFFFF8A65)),
    listOf(Color(0xFF00BCD4), Color(0xFF80DEEA)),
    listOf(Color(0xFF673AB7), Color(0xFF9575CD)),
    listOf(Color(0xFFCDDC39), Color(0xFFE6EE9C)),
    listOf(Color(0xFF2196F3), Color(0xFF90CAF9)),
)

// Emoji icons for common genres
private fun genreEmoji(genre: String): String {
    return when (genre.lowercase()) {
        "ação" -> "⚔️"
        "aventura" -> "🏔️"
        "comédia" -> "😂"
        "fantasia" -> "🧙"
        "shounen" -> "💪"
        "drama" -> "🎭"
        "ficção científica", "sci-fi" -> "🚀"
        "sobrenatural" -> "👻"
        "romance" -> "💕"
        "mistério" -> "🔍"
        "escolar" -> "🏫"
        "magia" -> "✨"
        "superpoder", "super poderes" -> "⚡"
        "seinen" -> "🎯"
        "kodomo" -> "🧒"
        "slice of life" -> "☀️"
        "animação" -> "🎬"
        "tokusatsu" -> "🦸"
        "mecha" -> "🤖"
        "musical" -> "🎵"
        "esporte", "esportes" -> "⚽"
        "horror", "terror" -> "💀"
        "psicológico" -> "🧠"
        else -> "🎌"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(
    onBackClick: () -> Unit,
    onGenreClick: (String) -> Unit,
    viewModel: GenresViewModel = hiltViewModel()
) {
    val genres by viewModel.genres.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gêneros", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    com.animebr.app.ui.common.AppBackButton(onClick = onBackClick)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(genres.size) { index ->
                val genre = genres[index]
                val gradient = genreGradients[index % genreGradients.size]
                GenreCard(
                    genre = genre,
                    emoji = genreEmoji(genre),
                    gradientColors = gradient,
                    onClick = { onGenreClick(genre) }
                )
            }
        }
    }
}

@Composable
fun GenreCard(
    genre: String,
    emoji: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors = gradientColors))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Text(
                text = genre,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}
