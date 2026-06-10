package com.animebr.app.ui.genres

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Anime
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GenreDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: AnimeRepository
) : ViewModel() {

    val genreName: String = savedStateHandle["genreName"] ?: ""

    val animes: StateFlow<List<Anime>> = repository.getAnimesByGenre(genreName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
