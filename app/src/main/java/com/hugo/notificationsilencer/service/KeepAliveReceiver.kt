package com.hugo.notificationsilencer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class KeepAliveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!hasNotificationAccess(context)) return
        Log.i(TAG, "Requesting notification listener rebind action=${intent.action}")
        requestNotificationListenerRebind(context)
    }

    companion object {
        private const val TAG = "SilencerKeepAlive"
    }
}
