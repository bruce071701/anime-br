package com.animebr.app.ui.dubsub

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.db.AnimeDao
import com.animebr.app.data.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DubSubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val animeDao: AnimeDao
) : ViewModel() {

    // "dublado" or "legendado"
    val type: String = savedStateHandle["type"] ?: "dublado"
    val isDub: Boolean = type == "dublado"

    private val _animes = MutableStateFlow<List<Anime>>(emptyList())
    val animes: StateFlow<List<Anime>> = _animes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 0
    private var hasMore = true
    private val pageSize = 30

    init {
        loadMore()
    }

    fun loadMore() {
        if (_isLoading.value || !hasMore) return
        viewModelScope.launch {
            _isLoading.value = true
            val offset = currentPage * pageSize
            val newItems = if (isDub) {
                animeDao.getDubbed(pageSize, offset)
            } else {
                animeDao.getSubbed(pageSize, offset)
            }
            if (newItems.size < pageSize) hasMore = false
            _animes.value = _animes.value + newItems
            currentPage++
            _isLoading.value = false
        }
    }
}
