package com.animebr.app.ui.episodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Anime
import com.animebr.app.data.model.Episode
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EpisodeListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnimeRepository
) : ViewModel() {

    val animeId: Int = savedStateHandle["animeId"] ?: 0

    private val _anime = MutableStateFlow<Anime?>(null)
    val anime: StateFlow<Anime?> = _anime.asStateFlow()

    private val _isAscending = MutableStateFlow(false)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

    // All episodes (sorted)
    private val allEpisodes: StateFlow<List<Episode>> = _isAscending
        .flatMapLatest { ascending ->
            repository.getEpisodesByAnimeId(animeId, ascending)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Page groups: ["1-50", "51-100", ...] 
    companion object {
        const val PAGE_SIZE = 50
    }

    val pageGroups: StateFlow<List<String>> = combine(allEpisodes, _isAscending) { episodes, ascending ->
        if (episodes.size <= PAGE_SIZE) emptyList()
        else {
            val totalPages = (episodes.size + PAGE_SIZE - 1) / PAGE_SIZE
            (0 until totalPages).map { page ->
                val start = page * PAGE_SIZE
                val end = minOf(start + PAGE_SIZE - 1, episodes.size - 1)
                // Use actual episode numbers from the sorted list
                val firstNum = episodes[start].nums?.toIntOrNull() ?: (start + 1)
                val lastNum = episodes[end].nums?.toIntOrNull() ?: (end + 1)
                val lo = minOf(firstNum, lastNum)
                val hi = maxOf(firstNum, lastNum)
                "EP $lo-$hi"
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current selected page index (-1 means show all if total <= PAGE_SIZE)
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    // Episodes for current page
    val episodes: StateFlow<List<Episode>> = combine(allEpisodes, _currentPage) { all, page ->
        if (all.size <= PAGE_SIZE) all
        else {
            val start = page * PAGE_SIZE
            val end = minOf(start + PAGE_SIZE, all.size)
            all.subList(start, end)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _anime.value = repository.getAnimeById(animeId)
        }
    }

    fun toggleSortOrder() {
        _isAscending.value = !_isAscending.value
        _currentPage.value = 0
    }

    fun selectPage(index: Int) {
        _currentPage.value = index
    }
}
