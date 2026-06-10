package com.animebr.app.ui.allanimes

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
class AllAnimesViewModel @Inject constructor(
    repository: AnimeRepository
) : ViewModel() {

    val allAnimes: StateFlow<List<Anime>> = repository.getAllAnimesAlphabetical()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
