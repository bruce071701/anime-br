package com.animebr.app.ui.genres

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Predefined gradient colors for genre cards
private val genreGradients = listOf(
    listOf(Color(0xFFFF6B35), Color(0xFFFF8F65)),
    listOf(Color(0xFF6C63FF), Color(0xFF8B83FF)),
    listOf(Color(0xFF00BFA5), Color(0xFF4DD0B8)),
    listOf(Color(0xFFE91E63), Color(0xFFFF5C8D)),
    listOf(Color(0xFF3F51B5), Color(0xFF7986CB)),
    listOf(Color(0xFFFF9800), Color(0xFFFFB74D)),
    listOf(Color(0xFF009688), Color(0xFF4DB6AC)),
    listOf(Color(0xFF8E24AA), Color(0xFFBA68C8)),
    listOf(Color(0xFF1976D2), Color(0xFF64B5F6)),
    listOf(Color(0xFFF44336), Color(0xFFEF5350)),
    listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
    listOf(Color(0xFF795548), Color(0xFFA1887F)),
    listOf(Color(0xFF607D8B), Color(0xFF90A4AE)),
    listOf(Color(0xFFCDDC39), Color(0xFFD4E157)),
    listOf(Color(0xFF00BCD4), Color(0xFF4DD0E1)),
    listOf(Color(0xFFFF5722), Color(0xFFFF8A65)),
)

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
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(colors = gradientColors)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = genre,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
