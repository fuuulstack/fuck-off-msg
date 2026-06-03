package com.hugo.notificationsilencer.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hugo.notificationsilencer.data.SilencerRepository
import com.hugo.notificationsilencer.ui.apps.AppsScreen
import com.hugo.notificationsilencer.ui.history.HistoryScreen
import com.hugo.notificationsilencer.ui.mark.MarkScreen
import com.hugo.notificationsilencer.ui.rules.RulesScreen
import com.hugo.notificationsilencer.ui.settings.SettingsScreen

enum class AppDestination(val route: String, val label: String) {
    History("history", "历史"),
    Apps("apps", "应用"),
    Rules("rules", "规则"),
    Settings("settings", "设置"),
}

@Composable
fun AppNav(repository: SilencerRepository) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.History) }
    var markingRecordId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (markingRecordId != null) {
        MarkScreen(
            record = repository.history().first { it.id == markingRecordId },
            onBack = { markingRecordId = null },
            onAddWhitelist = { keywords, scope ->
                repository.addKeywords(keywords, allow = true, scope = scope)
            },
            onAddBlacklist = { keywords, scope ->
                repository.addKeywords(keywords, allow = false, scope = scope)
            },
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        label = { Text(item.label) },
                        icon = { Text(item.label.take(1)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (destination) {
            AppDestination.History -> HistoryScreen(
                records = repository.history(),
                onMark = { markingRecordId = it.id },
                modifier = Modifier.padding(innerPadding),
            )
            AppDestination.Apps -> AppsScreen(
                summaries = repository.appSummaries(),
                modifier = Modifier.padding(innerPadding),
            )
            AppDestination.Rules -> RulesScreen(
                rules = repository.rules(),
                modifier = Modifier.padding(innerPadding),
            )
            AppDestination.Settings -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
