package com.animebr.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.model.Anime
import com.animebr.app.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    val favoriteAnimes: StateFlow<List<Anime>> =
        combine(
            repository.getAllFavorites(),
            repository.getAllAnimes()
        ) { favorites, animes ->
            val animeMap = animes.associateBy { it.id }
            favorites.mapNotNull { fav -> animeMap[fav.animeId] }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
