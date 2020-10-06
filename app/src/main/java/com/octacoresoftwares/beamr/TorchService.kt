package com.octacoresoftwares.beamr

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class TorchService : Service() {
    private lateinit var torchAndDetector: TorchAndDetector

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null && intent.hasExtra(INTENSITY_EXTRA_KEY)) {
            val value = intent.getStringExtra(INTENSITY_EXTRA_KEY)
            Log.i("TorchService", "Intent value: $value")
            if (value != null)
                torchAndDetector.setSensitivity(getIntensity(value))
        }

        torchAndDetector.startDetector()
        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        torchAndDetector = TorchAndDetector(this)
        torchAndDetector.initTorchAndDetector()
    }

    private fun getIntensity(value: String): Intensity {
        val array = resources.getStringArray(R.array.sensitivity_entries)
        return when (value) {
            array[0] -> Intensity.LOW
            array[1] -> Intensity.MEDIUM
            array[2] -> Intensity.HIGH
            else -> Intensity.MEDIUM
        }
    }

    override fun onDestroy() {
        torchAndDetector.stopDetector()
        super.onDestroy()
    }

    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(NOTIF_CHANNEL_ID, "Beamr Background Service")
            } else {
                ""
            }

        startForeground(
            NOTIF_ID, NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .build()
        )
    }

    @Suppress("SameParameterValue")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}

private const val NOTIF_ID = 100
private const val NOTIF_CHANNEL_ID = "Beamr"

const val INTENSITY_EXTRA_KEY = "intensity"