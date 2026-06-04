package com.hugo.notificationsilencer.service

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object NotificationListenerHealth {
    var connected by mutableStateOf(false)
    var lastConnectedAt by mutableStateOf<Long?>(null)
    var lastNotificationAt by mutableStateOf<Long?>(null)

    fun markConnected() {
        connected = true
        lastConnectedAt = System.currentTimeMillis()
    }

    fun markDisconnected() {
        connected = false
    }

    fun markNotificationReceived() {
        lastNotificationAt = System.currentTimeMillis()
    }
}

fun hasNotificationAccess(context: Context): Boolean {
    val componentName = ComponentName(context, NotificationListener::class.java)
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ).orEmpty()
    return enabledListeners.split(':').any { it == componentName.flattenToString() }
}

fun requestNotificationListenerRebind(context: Context) {
    NotificationListenerService.requestRebind(
        ComponentName(context, NotificationListener::class.java),
    )
}
