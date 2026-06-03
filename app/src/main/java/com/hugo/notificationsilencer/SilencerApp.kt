package com.hugo.notificationsilencer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hugo.notificationsilencer.data.PersistentSilencerRepository
import com.hugo.notificationsilencer.navigation.AppNav
import com.hugo.notificationsilencer.theme.NotificationSilencerTheme

@Composable
fun SilencerApp() {
    val context = LocalContext.current
    val repository = remember { PersistentSilencerRepository(context.applicationContext) }
    NotificationSilencerTheme {
        AppNav(repository = repository)
    }
}
