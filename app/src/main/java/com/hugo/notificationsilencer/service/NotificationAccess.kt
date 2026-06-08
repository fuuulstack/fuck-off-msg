package com.hugo.notificationsilencer.service

import android.content.ComponentName
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
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

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
}

fun openBatteryOptimizationSettings(context: Context) {
    runCatching {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}"),
            ),
        )
    }.recoverCatching { error ->
        if (error is ActivityNotFoundException) {
            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        } else {
            throw error
        }
    }.recoverCatching { error ->
        if (error is ActivityNotFoundException) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        } else {
            throw error
        }
    }
}
