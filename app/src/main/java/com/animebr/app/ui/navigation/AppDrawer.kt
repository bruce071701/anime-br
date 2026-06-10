package com.animebr.app.ui.navigation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DrawerItem(
    val label: String,
    val route: String?
) {
    INICIO("Inicio", NavRoutes.Home.route),
    BUSCAR("Buscar", NavRoutes.Search.route),
    ANIMES("Animes", NavRoutes.AllAnimes.route),
    GENEROS("Gêneros", NavRoutes.Genres.route),
    POPULARES("Populares", NavRoutes.Popular.route),
    FILMES("Filmes", NavRoutes.Movies.route),
    FAVORITOS("Favoritos", NavRoutes.Favorites.route),
    HISTORICO("Histórico", NavRoutes.History.route),
    DOWNLOADS("Downloads", NavRoutes.Downloads.route),
    MEU("Meu", NavRoutes.Me.route),
    DUBLADO("Dublado", NavRoutes.Dublado.route),
    LEGENDADO("Legendado", NavRoutes.Legendado.route),
    COMPARTILHAR("Compartilhar", null),
}

@Composable
fun AppDrawer(
    currentRoute: String?,
    onItemClick: (DrawerItem) -> Unit
) {
    val context = LocalContext.current

    ModalDrawerSheet(
        drawerContainerColor = Color.Black,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            // Header
            Text(
                text = "Navegar",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Menu items
            DrawerMenuItem(
                label = "Inicio",
                icon = Icons.Default.Home,
                selected = currentRoute == NavRoutes.Home.route,
                onClick = { onItemClick(DrawerItem.INICIO) }
            )
            DrawerMenuItem(
                label = "Buscar",
                icon = Icons.Default.Search,
                selected = currentRoute == NavRoutes.Search.route,
                onClick = { onItemClick(DrawerItem.BUSCAR) }
            )
            DrawerMenuItem(
                label = "Animes",
                icon = Icons.AutoMirrored.Filled.List,
                selected = currentRoute == NavRoutes.AllAnimes.route,
                onClick = { onItemClick(DrawerItem.ANIMES) }
            )
            DrawerMenuItem(
                label = "Gêneros",
                icon = Icons.Default.GridView,
                selected = currentRoute == NavRoutes.Genres.route,
                onClick = { onItemClick(DrawerItem.GENEROS) }
            )
            DrawerMenuItem(
                label = "Populares",
                icon = Icons.Default.Star,
                selected = currentRoute == NavRoutes.Popular.route,
                onClick = { onItemClick(DrawerItem.POPULARES) }
            )
            DrawerMenuItem(
                label = "Filmes",
                icon = Icons.Default.Movie,
                selected = currentRoute == NavRoutes.Movies.route,
                onClick = { onItemClick(DrawerItem.FILMES) }
            )
            DrawerMenuItem(
                label = "Favoritos",
                icon = Icons.Default.Favorite,
                selected = currentRoute == NavRoutes.Favorites.route,
                onClick = { onItemClick(DrawerItem.FAVORITOS) }
            )
            DrawerMenuItem(
                label = "Histórico",
                icon = Icons.Default.History,
                selected = currentRoute == NavRoutes.History.route,
                onClick = { onItemClick(DrawerItem.HISTORICO) }
            )
            DrawerMenuItem(
                label = "Downloads",
                icon = Icons.Default.Download,
                selected = currentRoute == NavRoutes.Downloads.route,
                onClick = { onItemClick(DrawerItem.DOWNLOADS) }
            )
            DrawerMenuItem(
                label = "Meu",
                icon = Icons.Default.Person,
                selected = currentRoute == NavRoutes.Me.route,
                onClick = { onItemClick(DrawerItem.MEU) }
            )
            DrawerMenuItem(
                label = "Dublado",
                icon = Icons.Default.Movie,
                selected = currentRoute == NavRoutes.Dublado.route,
                onClick = { onItemClick(DrawerItem.DUBLADO) }
            )
            DrawerMenuItem(
                label = "Legendado",
                icon = Icons.Default.Movie,
                selected = currentRoute == NavRoutes.Legendado.route,
                onClick = { onItemClick(DrawerItem.LEGENDADO) }
            )
            DrawerMenuItem(
                label = "Compartilhar",
                icon = Icons.Default.Share,
                selected = false,
                onClick = { onItemClick(DrawerItem.COMPARTILHAR) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Version
            Text(
                text = "Versão: ${getAppVersion(context)}",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF2A2A2A),
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

fun getAppVersion(context: Context): String {
    return try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }
}

fun shareApp(context: Context) {
    val shareText = "Todos os animes são 100% gratuitos para assistir! https://play.google.com/store/apps/details?id=br.anime.tv.animetv.animes.online"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar"))
}
