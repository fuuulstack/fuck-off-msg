package com.hugo.notificationsilencer

import android.content.ComponentName
import android.os.Bundle
import android.service.notification.NotificationListenerService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hugo.notificationsilencer.service.NotificationListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationListenerService.requestRebind(
            ComponentName(this, NotificationListener::class.java),
        )
        setContent {
            SilencerApp()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && !AppTaskVisibility.consumeSkipNextTaskRemoval()) {
            finishAndRemoveTask()
        }
    }
}
