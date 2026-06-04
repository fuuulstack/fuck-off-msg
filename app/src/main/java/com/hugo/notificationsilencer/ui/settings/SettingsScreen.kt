package com.hugo.notificationsilencer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
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
import com.hugo.notificationsilencer.ui.components.GlassBackground
import com.hugo.notificationsilencer.ui.components.GlassCard
import com.hugo.notificationsilencer.ui.components.PageHeader

@Composable
fun SettingsScreen(
    notificationAccessGranted: Boolean,
    notificationListenerConnected: Boolean,
    onOpenNotificationAccessSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PageHeader(
                title = "设置",
                subtitle = "权限、系统限制和增强模式",
                icon = Icons.Filled.Settings,
            )
            SettingsRow(
                icon = if (notificationAccessGranted && notificationListenerConnected) {
                    Icons.Filled.NotificationsActive
                } else {
                    Icons.Filled.NotificationsOff
                },
                title = "通知监听权限",
                body = when {
                    !notificationAccessGranted ->
                        "未开启。需要在系统设置中允许本 App 读取通知，拦截和历史记录才会生效。"
                    !notificationListenerConnected ->
                        "授权已开启，但监听服务未连接。请进入系统通知使用权页面，将本 App 关开一次。"
                    else ->
                        "已开启且监听服务已连接。系统会把新通知发送给本 App，用于拦截和写入历史。"
                },
                iconTint = if (notificationAccessGranted && notificationListenerConnected) MistBlue else MistRed,
                action = {
                    Button(onClick = onOpenNotificationAccessSettings) {
                        Text(if (notificationAccessGranted) "重新授权" else "去授权")
                    }
                },
            )
            SettingsRow(
                icon = Icons.Filled.Info,
                title = "Android 系统限制",
                body = "普通 Android 设备会尽快取消命中通知，但不同厂商系统可能会延迟回调，不能保证总是在铃声或震动前完成。",
            )
            SettingsRow(
                icon = Icons.Filled.VerifiedUser,
                title = "Shizuku 增强模式",
                body = "暂未启用。当前版本先保持普通通知监听方案，减少额外授权成本。",
            )
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
            }
            action?.invoke()
        }
    }
}
