package com.hugo.notificationsilencer.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hugo.notificationsilencer.data.SilencerRepository
import com.hugo.notificationsilencer.theme.AppBackground
import com.hugo.notificationsilencer.theme.GlassWhite
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText
import com.hugo.notificationsilencer.ui.apps.AppsScreen
import com.hugo.notificationsilencer.ui.history.HistoryScreen
import com.hugo.notificationsilencer.ui.mark.MarkScreen
import com.hugo.notificationsilencer.ui.rules.RulesScreen
import com.hugo.notificationsilencer.ui.settings.SettingsScreen

enum class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    History("history", "历史", Icons.Filled.History),
    Apps("apps", "应用", Icons.Filled.Apps),
    Rules("rules", "规则", Icons.AutoMirrored.Filled.Rule),
    Settings("settings", "设置", Icons.Filled.Settings),
}

@Composable
fun AppNav(
    repository: SilencerRepository,
    notificationAccessGranted: Boolean,
    notificationListenerConnected: Boolean,
    onOpenNotificationAccessSettings: () -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.History) }
    var markingRecordId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (markingRecordId != null) {
        MarkScreen(
            record = repository.history().first { it.id == markingRecordId },
            onBack = { markingRecordId = null },
            onAddWhitelist = { keywords, scope ->
                val record = repository.history().first { it.id == markingRecordId }
                repository.addKeywords(
                    keywords = keywords,
                    allow = true,
                    scope = scope,
                    packageName = record.packageName,
                    appName = record.appName,
                )
            },
            onAddBlacklist = { keywords, scope ->
                val record = repository.history().first { it.id == markingRecordId }
                repository.addKeywords(
                    keywords = keywords,
                    allow = false,
                    scope = scope,
                    packageName = record.packageName,
                    appName = record.appName,
                )
            },
        )
        return
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            NavigationBar(
                containerColor = GlassWhite,
                tonalElevation = NavigationBarDefaults.Elevation,
            ) {
                AppDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        label = { Text(item.label) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryText,
                            selectedTextColor = PrimaryText,
                            indicatorColor = Color.White.copy(alpha = 0.9f),
                            unselectedIconColor = SecondaryText,
                            unselectedTextColor = SecondaryText,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        when (destination) {
            AppDestination.History -> HistoryScreen(
                records = repository.history(),
                onMark = { markingRecordId = it.id },
                onDelete = { repository.deleteHistoryRecord(it.id) },
                onClearHistory = { repository.clearHistory() },
                notificationAccessGranted = notificationAccessGranted,
                notificationListenerConnected = notificationListenerConnected,
                onOpenNotificationAccessSettings = onOpenNotificationAccessSettings,
                modifier = Modifier.padding(innerPadding),
            )
            AppDestination.Apps -> AppsScreen(
                summaries = repository.appSummaries(),
                records = repository.history(),
                onMark = { markingRecordId = it.id },
                onDelete = { repository.deleteHistoryRecord(it.id) },
                modifier = Modifier.padding(innerPadding),
            )
            AppDestination.Rules -> RulesScreen(
                rules = repository.rules(),
                enhancedMarketingRulesEnabled = repository.settings().enhancedMarketingRulesEnabled,
                onEnhancedMarketingRulesEnabledChange = repository::setEnhancedMarketingRulesEnabled,
                onDeleteRules = repository::deleteRules,
                modifier = Modifier.padding(innerPadding),
            )
            AppDestination.Settings -> SettingsScreen(
                notificationAccessGranted = notificationAccessGranted,
                notificationListenerConnected = notificationListenerConnected,
                onOpenNotificationAccessSettings = onOpenNotificationAccessSettings,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
