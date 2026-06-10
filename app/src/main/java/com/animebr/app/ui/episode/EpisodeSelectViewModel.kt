package com.animebr.app.ui.episode

import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.download.DownloadManager
import com.animebr.app.data.model.Anime
import com.animebr.app.data.model.Episode
import com.animebr.app.data.model.Player
import com.animebr.app.data.player.BloggerVideoExtractor
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeSelectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnimeRepository,
    private val downloadManager: DownloadManager,
    private val bloggerExtractor: BloggerVideoExtractor,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val animeId: Int = savedStateHandle["animeId"] ?: 0
    private val episodeId: Int = savedStateHandle["episodeId"] ?: 0

    fun getEpisodeId(): Int = episodeId

    private val _anime = MutableStateFlow<Anime?>(null)
    val anime: StateFlow<Anime?> = _anime.asStateFlow()

    private val _episode = MutableStateFlow<Episode?>(null)
    val episode: StateFlow<Episode?> = _episode.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            _anime.value = repository.getAnimeById(animeId)
            _episode.value = repository.getEpisodeById(episodeId)
            fetchPlayers()
        }
    }

    private suspend fun fetchPlayers() {
        _isLoading.value = true
        _error.value = null
        try {
            val result = repository.fetchPlayers(episodeId)
            _players.value = result
            if (result.isEmpty()) {
                _error.value = "Nenhum servidor disponível para este episódio"
            }
        } catch (e: Exception) {
            _error.value = "Erro ao carregar servidores: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun retry() {
        viewModelScope.launch { fetchPlayers() }
    }

    fun downloadEpisode(player: Player) {
        val link = player.link
        if (link.isNullOrBlank()) {
            Toast.makeText(context, "Link não disponível", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            Toast.makeText(context, "Preparando download...", Toast.LENGTH_SHORT).show()

            // Resolve actual downloadable URL (same logic as player)
            val downloadUrl = resolveDownloadUrl(link)
            if (downloadUrl == null) {
                Toast.makeText(context, "Não foi possível obter o link", Toast.LENGTH_SHORT).show()
                return@launch
            }

            downloadManager.startDownload(
                animeId = animeId,
                episodeId = episodeId,
                animeName = _anime.value?.name,
                episodeTitle = _episode.value?.title,
                episodeNumber = _episode.value?.nums?.toIntOrNull() ?: 0,
                videoUrl = downloadUrl
            )
            _showDownloadToast.value = true
        }
    }

    private suspend fun resolveDownloadUrl(link: String): String? {
        return when {
            // Direct m3u8 - can't easily download, use as-is
            link.contains(".m3u8") -> link
            // Direct MP4
            link.matches(Regex(".*\\.(mp4|MP4)$")) ||
            (link.contains(".mp4") && !link.contains("bg.mp4") && !link.contains("animesdigital.org")) ||
            (link.contains(".MP4") && !link.contains("bg.mp4") && !link.contains("animesdigital.org")) -> link
            // Blogger video.g - extract googlevideo URL
            link.contains("blogger.com/video.g") -> bloggerExtractor.extractVideoUrl(link)
            // animesdigital.org with blogger
            link.contains("animesdigital.org") && link.contains("bg.mp4") -> {
                // Decode base64 blogger URL and extract
                try {
                    val pathPart = link.substringAfter("animesdigital.org/")
                    val segments = pathPart.split("/")
                    if (segments.size < 3) return link
                    val base64Part = segments[0]
                    val episodeIndex = segments[1].toIntOrNull() ?: 0
                    val bloggerUrl = String(android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)).trim()

                    val client = okhttp3.OkHttpClient.Builder().followRedirects(true).build()
                    val request = okhttp3.Request.Builder().url(bloggerUrl)
                        .header("User-Agent", "Mozilla/5.0").build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string() ?: return link

                    val pattern = Regex("""src="(https://www\.blogger\.com/video\.g\?token=[^"&]+)""")
                    val matches = pattern.findAll(body).toList()
                    if (matches.isEmpty()) return link

                    val targetIndex = episodeIndex.coerceIn(0, matches.size - 1)
                    val videoGUrl = matches[targetIndex].groupValues[1].replace("&amp;", "&")
                    bloggerExtractor.extractVideoUrl(videoGUrl)
                } catch (e: Exception) {
                    link
                }
            }
            // Default
            else -> link
        }
    }

    private val _showDownloadToast = MutableStateFlow(false)
    val showDownloadToast: StateFlow<Boolean> = _showDownloadToast.asStateFlow()

    fun dismissDownloadToast() {
        _showDownloadToast.value = false
    }
}
