package com.animebr.app.data.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val CHANNEL_NAME = "Downloads"
        const val ACTION_OPEN_DOWNLOADS = "com.animebr.app.OPEN_DOWNLOADS"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress"
                setShowBadge(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getContentIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            action = ACTION_OPEN_DOWNLOADS
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        } ?: Intent()
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showProgress(downloadId: Int, title: String, progress: Int, indeterminate: Boolean = false) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(getContentIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (indeterminate) {
            builder.setProgress(0, 0, true)
            builder.setContentText("Baixando...")
        } else {
            builder.setProgress(100, progress, false)
            builder.setContentText("$progress%")
        }

        try {
            NotificationManagerCompat.from(context).notify(downloadId, builder.build())
        } catch (e: SecurityException) {
            // No notification permission
        }
    }

    fun showComplete(downloadId: Int, title: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText("Download concluído")
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(getContentIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)

        try {
            NotificationManagerCompat.from(context).notify(downloadId, builder.build())
        } catch (e: SecurityException) {
            // No notification permission
        }
    }

    fun showFailed(downloadId: Int, title: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText("Download falhou")
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(getContentIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)

        try {
            NotificationManagerCompat.from(context).notify(downloadId, builder.build())
        } catch (e: SecurityException) {
            // No notification permission
        }
    }

    fun cancel(downloadId: Int) {
        NotificationManagerCompat.from(context).cancel(downloadId)
    }
}
