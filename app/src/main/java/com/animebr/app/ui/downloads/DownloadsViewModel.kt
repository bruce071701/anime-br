package com.animebr.app.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animebr.app.data.db.DownloadDao
import com.animebr.app.data.download.DownloadManager
import com.animebr.app.data.model.Download
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadManager: DownloadManager,
    private val downloadDao: DownloadDao
) : ViewModel() {

    private val _downloads = MutableStateFlow<List<Download>>(emptyList())
    val downloads: StateFlow<List<Download>> = _downloads.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                try {
                    _downloads.value = downloadDao.getAllDownloadsSync()
                } catch (e: Exception) { /* ignore */ }
                delay(800) // Refresh every 800ms
            }
        }
    }

    fun pauseDownload(downloadId: Int) {
        downloadManager.pauseDownload(downloadId)
    }

    fun resumeDownload(downloadId: Int) {
        downloadManager.resumeDownload(downloadId)
    }

    fun deleteDownload(downloadId: Int) {
        viewModelScope.launch {
            downloadManager.deleteDownload(downloadId)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            downloadManager.deleteAllDownloads()
        }
    }
}
