package com.hugo.notificationsilencer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.theme.MistBlue
import com.hugo.notificationsilencer.theme.MistRed
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText
import com.hugo.notificationsilencer.data.AppLanguage
import com.hugo.notificationsilencer.ui.components.GlassBackground
import com.hugo.notificationsilencer.ui.components.GlassCard
import com.hugo.notificationsilencer.ui.components.PageHeader
import com.hugo.notificationsilencer.ui.i18n.LocalSilencerStrings

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    notificationAccessGranted: Boolean,
    notificationListenerConnected: Boolean,
    ignoringBatteryOptimizations: Boolean,
    appLanguage: AppLanguage,
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenBatteryOptimizationSettings: () -> Unit,
    onAppLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalSilencerStrings.current
    GlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = strings.settings,
                    subtitle = strings.settingsSubtitle,
                    icon = Icons.Filled.Settings,
                )
            }
            item {
                SettingsRow(
                    icon = if (notificationAccessGranted && notificationListenerConnected) {
                        Icons.Filled.NotificationsActive
                    } else {
                        Icons.Filled.NotificationsOff
                    },
                    title = strings.notificationPermission,
                    body = when {
                        !notificationAccessGranted ->
                            strings.notificationAccessMissingBody
                        !notificationListenerConnected ->
                            strings.notificationAccessDisconnectedBody
                        else ->
                            strings.notificationAccessOkBody
                    },
                    iconTint = if (notificationAccessGranted && notificationListenerConnected) MistBlue else MistRed,
                    action = {
                        Button(onClick = onOpenNotificationAccessSettings) {
                            Text(if (notificationAccessGranted) strings.reauthorize else strings.authorize)
                        }
                    },
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = strings.androidLimits,
                    body = strings.androidLimitsBody,
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Filled.BatterySaver,
                    title = strings.keepAlive,
                    body = if (ignoringBatteryOptimizations) {
                        strings.keepAliveEnabledBody
                    } else {
                        strings.keepAliveDisabledBody
                    },
                    iconTint = if (ignoringBatteryOptimizations) MistBlue else MistRed,
                    action = {
                        Button(onClick = onOpenBatteryOptimizationSettings) {
                            Text(if (ignoringBatteryOptimizations) strings.configured else strings.goSet)
                        }
                    },
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = strings.language,
                    body = strings.languageBody,
                    action = null,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        LanguageChip(
                            selected = appLanguage == AppLanguage.System,
                            label = strings.languageSystem,
                            onClick = { onAppLanguageChange(AppLanguage.System) },
                        )
                        LanguageChip(
                            selected = appLanguage == AppLanguage.English,
                            label = strings.languageEnglish,
                            onClick = { onAppLanguageChange(AppLanguage.English) },
                        )
                        LanguageChip(
                            selected = appLanguage == AppLanguage.Chinese,
                            label = strings.languageChinese,
                            onClick = { onAppLanguageChange(AppLanguage.Chinese) },
                        )
                    }
                }
            }
            item {
                SettingsRow(
                    icon = Icons.Filled.VerifiedUser,
                    title = strings.shizukuMode,
                    body = strings.shizukuModeBody,
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    body: String,
    iconTint: androidx.compose.ui.graphics.Color = MistBlue,
    action: (@Composable () -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                extraContent?.invoke()
            }
            action?.invoke()
        }
    }
}

@Composable
private fun LanguageChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}
