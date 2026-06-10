package com.animebr.app.ui.popular

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
class PopularViewModel @Inject constructor(
    repository: AnimeRepository
) : ViewModel() {

    // Top 50 by visitas (view count, used as popularity proxy since voteAverage is 0)
    val popularAnimes: StateFlow<List<Anime>> = repository.getMostViewed(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
