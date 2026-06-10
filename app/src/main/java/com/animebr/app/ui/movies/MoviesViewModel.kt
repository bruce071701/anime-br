package com.animebr.app.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Anime
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    repository: AnimeRepository
) : ViewModel() {

    val movies: StateFlow<List<Anime>> = repository.getAllAnimes()
        .map { animes -> animes.filter { it.type == "Movie" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
