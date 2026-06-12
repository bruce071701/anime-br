package com.animebr.app

import android.app.Application
import com.animebr.app.data.sync.DataSyncManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AnimeBRApp : Application() {

    @Inject
    lateinit var dataSyncManager: DataSyncManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Silent background sync at startup
        appScope.launch {
            dataSyncManager.sync()
        }
    }
}
