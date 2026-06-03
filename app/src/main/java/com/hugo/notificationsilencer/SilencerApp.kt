package com.hugo.notificationsilencer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.hugo.notificationsilencer.theme.NotificationSilencerTheme

@Composable
fun SilencerApp() {
    NotificationSilencerTheme {
        Text(text = "通知静默")
    }
}
