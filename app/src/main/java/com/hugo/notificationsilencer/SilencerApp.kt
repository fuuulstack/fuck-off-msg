package com.hugo.notificationsilencer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.hugo.notificationsilencer.service.isIgnoringBatteryOptimizations
import com.hugo.notificationsilencer.service.openBatteryOptimizationSettings
import com.hugo.notificationsilencer.service.requestNotificationListenerRebind
import com.hugo.notificationsilencer.theme.NotificationSilencerTheme
import com.hugo.notificationsilencer.ui.i18n.LocalSilencerStrings
import com.hugo.notificationsilencer.ui.i18n.stringsFor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

@Composable
fun SilencerApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember { PersistentSilencerRepository(context.applicationContext) }
    var notificationAccessGranted by remember { mutableStateOf(hasNotificationAccess(context)) }
    var ignoringBatteryOptimizations by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }
    var healthCheckRequest by remember { mutableStateOf(0) }

    fun refreshNotificationAccess() {
        notificationAccessGranted = hasNotificationAccess(context)
        ignoringBatteryOptimizations = isIgnoringBatteryOptimizations(context)
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
        CompositionLocalProvider(LocalSilencerStrings provides stringsFor(repository.settings().appLanguage)) {
            AppNav(
                repository = repository,
                notificationAccessGranted = notificationAccessGranted,
                notificationListenerConnected = NotificationListenerHealth.connected,
                ignoringBatteryOptimizations = ignoringBatteryOptimizations,
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
                onOpenBatteryOptimizationSettings = {
                    AppTaskVisibility.skipNextTaskRemoval = true
                    openBatteryOptimizationSettings(context)
                },
            )
        }
    }
}
