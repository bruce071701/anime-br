package com.animebr.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.animebr.app.data.download.DownloadNotificationHelper
import com.animebr.app.ui.navigation.AppNavGraph
import com.animebr.app.ui.navigation.NavRoutes
import com.animebr.app.ui.rating.RatingManager
import com.animebr.app.ui.theme.AnimeBRTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var ratingManager: RatingManager

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            AnimeBRTheme {
                val nc = rememberNavController()
                navController = nc
                AppNavGraph(
                    navController = nc,
                    ratingManager = ratingManager,
                    onSplashFinished = {
                        // Check if opened from notification
                        handleIntent(intent)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == DownloadNotificationHelper.ACTION_OPEN_DOWNLOADS) {
            navController?.navigate(NavRoutes.Downloads.route) {
                launchSingleTop = true
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
