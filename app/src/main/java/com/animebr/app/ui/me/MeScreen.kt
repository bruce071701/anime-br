package com.animebr.app.ui.me

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    onBackClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDownloadsClick: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    com.animebr.app.ui.common.AppBackButton(onClick = onBackClick)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))) {
                Column {
                    MeMenuItem(Icons.Default.Favorite, Color(0xFFFF6B35), "Favoritos", "Meus animes favoritos", onFavoritesClick)
                    MeMenuItem(Icons.Default.History, Color(0xFF4CAF50), "Histórico", "Histórico de reprodução", onHistoryClick)
                    MeMenuItem(Icons.Default.Download, Color(0xFF2196F3), "Downloads", "Gerenciar downloads", onDownloadsClick, false)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))) {
                Column {
                    MeMenuItem(Icons.Default.Lock, Color.Gray, "Política de Privacidade", null, { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://animebr.app/privacy"))) })
                    MeMenuItem(Icons.Default.Description, Color.Gray, "Termos de Serviço", null, { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://animebr.app/terms"))) }, false)
                }
            }
        }
    }
}

@Composable
private fun MeMenuItem(icon: ImageVector, iconTint: Color, title: String, subtitle: String? = null, onClick: () -> Unit, showDivider: Boolean = true) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, title, tint = iconTint, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) Text(subtitle, color = Color(0xFFB0B0B0), fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF808080), modifier = Modifier.size(20.dp))
    }
}
