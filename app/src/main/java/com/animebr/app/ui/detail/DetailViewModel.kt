package com.animebr.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Anime
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnimeRepository
) : ViewModel() {

    val animeId: Int = savedStateHandle["animeId"] ?: 0

    private val _anime = MutableStateFlow<Anime?>(null)
    val anime: StateFlow<Anime?> = _anime.asStateFlow()

    val isFavorite: StateFlow<Boolean> = repository.isFavorite(animeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            _anime.value = repository.getAnimeById(animeId)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            if (isFavorite.value) {
                repository.removeFavorite(animeId)
            } else {
                repository.addFavorite(animeId)
            }
        }
    }
}
