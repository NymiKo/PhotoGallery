package com.easyprog.android.photogallery.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        Log.e("NOTIFICATION_RECEIVER", "received broadcast ${intent.action}")
    }
}