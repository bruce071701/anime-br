package com.animebr.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B35),
    secondary = Color(0xFFFF8A65),
    tertiary = Color(0xFFFFAB91),
    background = Color(0xFF000000),
    surface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFF2A2A2A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0)
)

@Composable
fun AnimeBRTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
