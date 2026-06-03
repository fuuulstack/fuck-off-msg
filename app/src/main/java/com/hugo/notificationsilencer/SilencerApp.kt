package com.hugo.notificationsilencer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hugo.notificationsilencer.data.InMemorySilencerRepository
import com.hugo.notificationsilencer.navigation.AppNav
import com.hugo.notificationsilencer.theme.NotificationSilencerTheme

@Composable
fun SilencerApp() {
    val repository = remember { InMemorySilencerRepository.sample() }
    NotificationSilencerTheme {
        AppNav(repository = repository)
    }
}
