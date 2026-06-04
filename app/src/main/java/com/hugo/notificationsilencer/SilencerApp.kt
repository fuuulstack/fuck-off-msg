package com.hugo.notificationsilencer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.hugo.notificationsilencer.data.PersistentSilencerRepository
import com.hugo.notificationsilencer.navigation.AppNav
import com.hugo.notificationsilencer.service.NotificationListenerHealth
import com.hugo.notificationsilencer.service.hasNotificationAccess
import com.hugo.notificationsilencer.service.requestNotificationListenerRebind
import com.hugo.notificationsilencer.theme.NotificationSilencerTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

@Composable
fun SilencerApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember { PersistentSilencerRepository(context.applicationContext) }
    var notificationAccessGranted by remember { mutableStateOf(hasNotificationAccess(context)) }
    var healthCheckRequest by remember { mutableStateOf(0) }

    fun refreshNotificationAccess() {
        notificationAccessGranted = hasNotificationAccess(context)
        if (notificationAccessGranted) {
            requestNotificationListenerRebind(context)
        }
        healthCheckRequest += 1
    }

    LaunchedEffect(healthCheckRequest) {
        if (healthCheckRequest == 0 || !notificationAccessGranted) return@LaunchedEffect
        delay(1500)
        if (!NotificationListenerHealth.connected) {
            requestNotificationListenerRebind(context)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshNotificationAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NotificationSilencerTheme {
        AppNav(
            repository = repository,
            notificationAccessGranted = notificationAccessGranted,
            notificationListenerConnected = NotificationListenerHealth.connected,
            onOpenNotificationAccessSettings = {
                AppTaskVisibility.skipNextTaskRemoval = true
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }.recoverCatching { error ->
                    if (error is ActivityNotFoundException) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    } else {
                        throw error
                    }
                }
            },
        )
    }
}
