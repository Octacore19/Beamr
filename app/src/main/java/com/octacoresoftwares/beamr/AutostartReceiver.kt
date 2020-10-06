package com.octacoresoftwares.beamr

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager


class AutostartReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val start = prefs.getBoolean(context.getString(R.string.service_switch_key), false)

        val torchIntent = Intent(context, TorchService::class.java)
        if (start) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(torchIntent)
            } else {
                context.startService(torchIntent)
            }
        }
    }
}
