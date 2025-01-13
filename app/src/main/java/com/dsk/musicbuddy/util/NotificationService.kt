package com.dsk.musicbuddy.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val serviceIntent = Intent(context, MusicService::class.java).apply { this.action = action }
        context.startService(serviceIntent)
    }
}