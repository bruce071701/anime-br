package com.animebr.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Anime
import com.animebr.app.data.model.WatchHistory
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryItem(
    val history: WatchHistory,
    val anime: Anime?
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    val historyItems: StateFlow<List<HistoryItem>> =
        combine(
            repository.getWatchHistory(100),
            repository.getAllAnimes()
        ) { history, animes ->
            val animeMap = animes.associateBy { it.id }
            history.map { h ->
                HistoryItem(history = h, anime = animeMap[h.animeId])
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}
