package com.animebr.app.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Episode
import com.animebr.app.data.model.WatchHistory
import com.animebr.app.data.player.BloggerVideoExtractor
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnimeRepository,
    private val bloggerExtractor: BloggerVideoExtractor,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val episodeId: Int = savedStateHandle["episodeId"] ?: 0
    private val sourceId: Int = savedStateHandle["sourceId"] ?: 0

    private val _videoUrl = MutableStateFlow<String?>(null)
    val videoUrl: StateFlow<String?> = _videoUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _animeName = MutableStateFlow("")
    val animeName: StateFlow<String> = _animeName.asStateFlow()

    private val _episodeTitle = MutableStateFlow("")
    val episodeTitle: StateFlow<String> = _episodeTitle.asStateFlow()

    private val _resumePosition = MutableStateFlow(0L)
    val resumePosition: StateFlow<Long> = _resumePosition.asStateFlow()

    // Episode list for episode selection
    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _currentEpisodeIndex = MutableStateFlow(-1)
    val currentEpisodeIndex: StateFlow<Int> = _currentEpisodeIndex.asStateFlow()

    // Show/hide episode selector panel
    private val _showEpisodeSelector = MutableStateFlow(false)
    val showEpisodeSelector: StateFlow<Boolean> = _showEpisodeSelector.asStateFlow()

    private var animeId: Int = 0
    private var episodeNumber: Int = 0
    private var serverType: String = ""
    private var currentEpisodeId: Int = 0
    private var allPlayers: List<com.animebr.app.data.model.Player> = emptyList()
    private var currentPlayerIndex: Int = 0

    init {
        currentEpisodeId = episodeId
        viewModelScope.launch {
            allPlayers = repository.fetchPlayers(episodeId)
            val player = allPlayers.find { it.id == sourceId } ?: allPlayers.firstOrNull()
            currentPlayerIndex = allPlayers.indexOf(player)
            animeId = player?.animeId ?: 0
            serverType = player?.server ?: ""

            val episode = repository.getEpisodeById(episodeId)
            episodeNumber = episode?.nums?.toIntOrNull() ?: 0
            _episodeTitle.value = episode?.title ?: "Episódio $episodeNumber"

            val anime = if (animeId > 0) repository.getAnimeById(animeId) else null
            _animeName.value = anime?.name ?: ""

            // Load episode list for this anime (ascending order)
            if (animeId > 0) {
                val episodeList = repository.getEpisodesByAnimeId(animeId, ascending = true).first()
                _episodes.value = episodeList
                _currentEpisodeIndex.value = episodeList.indexOfFirst { it.id == episodeId }
            }

            // Check watch history for resume position
            val history = repository.getHistoryForEpisode(animeId, episodeId)
            if (history != null) {
                if (history.duration > 0 && history.progress.toFloat() / history.duration > 0.95f) {
                    _resumePosition.value = 0L
                } else {
                    _resumePosition.value = history.progress
                }
            }

            // Check if there's a local downloaded file first
            val localFile = java.io.File(
                context.getExternalFilesDir(null), "downloads/ep_${currentEpisodeId}.mp4"
            )
            if (localFile.exists()) {
                _videoUrl.value = localFile.toURI().toString()
                _isLoading.value = false
                return@launch
            }

            // Resolve video URL - try current player
            tryPlayCurrentSource()
        }
    }

    private suspend fun tryPlayCurrentSource() {
        if (currentPlayerIndex < 0 || currentPlayerIndex >= allPlayers.size) {
            _isLoading.value = false
            return
        }
        val player = allPlayers[currentPlayerIndex]
        val link = player.link
        if (link != null) {
            resolveVideoUrl(link, player.server ?: "")
        } else {
            // This source has no link, try next
            tryNextSource()
        }
    }

    /**
     * Called when playback fails. Tries the next available source.
     * If all sources fail, leaves the error state.
     */
    fun tryNextSource() {
        viewModelScope.launch {
            currentPlayerIndex++
            if (currentPlayerIndex < allPlayers.size) {
                _videoUrl.value = null
                _isLoading.value = true
                _resumePosition.value = 0L
                tryPlayCurrentSource()
            }
            // else: all sources exhausted, error state remains
        }
    }

    fun toggleEpisodeSelector() {
        _showEpisodeSelector.value = !_showEpisodeSelector.value
    }

    fun hasPreviousEpisode(): Boolean {
        return _currentEpisodeIndex.value > 0
    }

    fun hasNextEpisode(): Boolean {
        val episodes = _episodes.value
        return _currentEpisodeIndex.value < episodes.size - 1
    }

    /**
     * Switch to a different episode. Fetches players for the new episode
     * and plays using the same server type preference.
     */
    fun switchEpisode(episode: Episode) {
        viewModelScope.launch {
            _isLoading.value = true
            _videoUrl.value = null
            _showEpisodeSelector.value = false

            currentEpisodeId = episode.id
            episodeNumber = episode.nums?.toIntOrNull() ?: 0
            _episodeTitle.value = episode.title ?: "Episódio $episodeNumber"
            _currentEpisodeIndex.value = _episodes.value.indexOfFirst { it.id == episode.id }
            _resumePosition.value = 0L

            // Check resume position for new episode
            val history = repository.getHistoryForEpisode(animeId, episode.id)
            if (history != null && history.duration > 0 && history.progress.toFloat() / history.duration <= 0.95f) {
                _resumePosition.value = history.progress
            }

            // Fetch players for new episode
            val players = repository.fetchPlayers(episode.id)
            // Prefer same server type, fallback to first available
            val player = players.find { it.server == serverType } ?: players.firstOrNull()

            val link = player?.link
            if (link != null) {
                resolveVideoUrl(link, player.server ?: "")
            } else {
                _isLoading.value = false
            }
        }
    }

    fun playPreviousEpisode() {
        val index = _currentEpisodeIndex.value
        if (index > 0) {
            switchEpisode(_episodes.value[index - 1])
        }
    }

    fun playNextEpisode() {
        val episodes = _episodes.value
        val index = _currentEpisodeIndex.value
        if (index < episodes.size - 1) {
            switchEpisode(episodes[index + 1])
        }
    }

    private suspend fun resolveVideoUrl(link: String, server: String) {
        try {
            when {
                // Direct m3u8 stream
                link.contains(".m3u8") -> {
                    _videoUrl.value = link
                }
                // Direct MP4 (not the animesdigital bg.mp4 pattern)
                link.matches(Regex(".*\\.(mp4|MP4)$")) ||
                (link.contains(".mp4") && !link.contains("bg.mp4") && !link.contains("animesdigital.org")) ||
                (link.contains(".MP4") && !link.contains("bg.mp4") && !link.contains("animesdigital.org")) -> {
                    _videoUrl.value = link
                }
                // Blogger video.g URL - extract via RPC
                link.contains("blogger.com/video.g") -> {
                    val directUrl = bloggerExtractor.extractVideoUrl(link)
                    _videoUrl.value = directUrl ?: link
                }
                // animesdigital.org pattern with base64 blogger URL
                link.contains("animesdigital.org") && link.contains("bg.mp4") -> {
                    val directUrl = extractPlayer2Url(link)
                    _videoUrl.value = directUrl ?: link
                }
                // Default: try direct playback
                else -> {
                    _videoUrl.value = link
                }
            }
        } catch (e: Exception) {
            _videoUrl.value = link
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * For player2: decode the blogger URL, find the video.g iframe, then use
     * BloggerVideoExtractor to get the googlevideo direct link.
     */
    private suspend fun extractPlayer2Url(link: String): String? {
        try {
            // Parse: animesdigital.org/{base64}/{episodeIndex}/bg.mp4?...
            val pathPart = link.substringAfter("animesdigital.org/")
            val segments = pathPart.split("/")
            if (segments.size < 3) return null

            val base64Part = segments[0]
            val episodeIndex = segments[1].toIntOrNull() ?: 0

            // Decode base64 to get blogger page URL
            val bloggerUrl = String(android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)).trim()

            // Fetch blogger page to find video.g iframes
            val client = okhttp3.OkHttpClient.Builder().followRedirects(true).build()
            val request = okhttp3.Request.Builder()
                .url(bloggerUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null

            // Find all video.g iframe URLs
            val pattern = Regex("""src="(https://www\.blogger\.com/video\.g\?token=[^"&]+)""")
            val matches = pattern.findAll(body).toList()
            if (matches.isEmpty()) return null

            val targetIndex = episodeIndex.coerceIn(0, matches.size - 1)
            val videoGUrl = matches[targetIndex].groupValues[1].replace("&amp;", "&")

            // Use BloggerVideoExtractor to get the googlevideo direct URL
            return bloggerExtractor.extractVideoUrl(videoGUrl)
        } catch (e: Exception) {
            return null
        }
    }

    fun saveProgress(progress: Long, duration: Long) {
        if (animeId == 0 || currentEpisodeId == 0) return
        viewModelScope.launch {
            repository.saveWatchProgress(
                WatchHistory(
                    animeId = animeId,
                    episodeId = currentEpisodeId,
                    episodeNumber = episodeNumber,
                    progress = progress,
                    duration = duration,
                    lastWatchedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
