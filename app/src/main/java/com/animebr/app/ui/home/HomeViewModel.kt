package com.animebr.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Anime
import com.animebr.app.data.model.Episode
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Represents a recent anime update item for the home grid.
 * Group by animeId, take the max episode number, order by createdAt desc.
 */
data class RecentAnimeItem(
    val anime: Anime,
    val latestEpisodeNumber: String,
    val latestCreatedAt: String?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Home page data: recent episodes grouped by animeId,
     * showing the latest episode number per anime, ordered by createdAt desc.
     * Limited to 50 items.
     */
    val recentAnimes: StateFlow<List<RecentAnimeItem>> =
        combine(
            repository.getLatestEpisodesGrouped(50),
            repository.getAllAnimes()
        ) { episodes, animes ->
            _isLoading.value = false
            val animeMap = animes.associateBy { it.id }
            episodes.mapNotNull { ep ->
                animeMap[ep.animeId]?.let { anime ->
                    RecentAnimeItem(
                        anime = anime,
                        latestEpisodeNumber = ep.nums ?: "?",
                        latestCreatedAt = ep.createdAt
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
