package com.hugo.notificationsilencer.ui.mark

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.NotificationRecord
import com.hugo.notificationsilencer.data.RuleScope
import com.hugo.notificationsilencer.theme.AddedToken
import com.hugo.notificationsilencer.theme.GlassBorder
import com.hugo.notificationsilencer.theme.MistGreen
import com.hugo.notificationsilencer.theme.MistGreenContainer
import com.hugo.notificationsilencer.theme.MistRed
import com.hugo.notificationsilencer.theme.MistRedContainer
import com.hugo.notificationsilencer.theme.MutedText
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText
import com.hugo.notificationsilencer.theme.SelectedToken
import com.hugo.notificationsilencer.ui.components.GlassActionRow
import com.hugo.notificationsilencer.ui.components.GlassBackground
import com.hugo.notificationsilencer.ui.components.GlassCard
import com.hugo.notificationsilencer.ui.components.IconLabelButton
import com.hugo.notificationsilencer.ui.components.StatusPill

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MarkScreen(
    record: NotificationRecord,
    onBack: () -> Unit,
    onAddWhitelist: (List<String>, RuleScope) -> Unit,
    onAddBlacklist: (List<String>, RuleScope) -> Unit,
) {
    val cells = remember(record.id) { TextCell.tokenize("${record.title}${record.body}") }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var added by remember { mutableStateOf(setOf<Int>()) }
    var pendingAllow by remember { mutableStateOf<Boolean?>(null) }

    fun selectedKeywords(): List<String> {
        return SmearSelection.groupSelectedKeywords(cells, selected)
    }

    pendingAllow?.let { allow ->
        val keywords = selectedKeywords()
        AlertDialog(
            onDismissRequest = { pendingAllow = null },
            title = { Text(if (allow) "添加白名单" else "添加黑名单") },
            text = {
                Text(
                    text = if (keywords.isEmpty()) "还没有选择文字" else keywords.joinToString("、"),
                    color = SecondaryText,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (allow) onAddWhitelist(keywords, RuleScope.Global)
                        else onAddBlacklist(keywords, RuleScope.Global)
                        added = added + selected
                        selected = emptySet()
                        pendingAllow = null
                    },
                    enabled = keywords.isNotEmpty(),
                ) {
                    Text(if (allow) "全局白名单" else "全局黑名单")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        if (allow) onAddWhitelist(keywords, RuleScope.CurrentApp)
                        else onAddBlacklist(keywords, RuleScope.CurrentApp)
                        added = added + selected
                        selected = emptySet()
                        pendingAllow = null
                    },
                    enabled = keywords.isNotEmpty(),
                ) {
                    Text(if (allow) "仅当前 App 白名单" else "仅当前 App 黑名单")
                }
            },
        )
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconLabelButton(
                    text = "返回",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack,
                    containerColor = Color.White.copy(alpha = 0.92f),
                    contentColor = PrimaryText,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "涂抹选词",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText,
                    )
                    Text(
                        text = "连续选择视为一个词，非连续选择视为多个词",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                    )
                }
            }

            SourceNotificationCard(record = record)

            FlowRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                cells.forEach { cell ->
                    val selectedNow = selected.contains(cell.index)
                    val addedNow = added.contains(cell.index)
                    Text(
                        text = cell.text,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    addedNow -> AddedToken
                                    selectedNow -> SelectedToken
                                    else -> Color.White.copy(alpha = 0.82f)
                                },
                            )
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .clickable(enabled = !addedNow) {
                                selected = if (selectedNow) selected - cell.index else selected + cell.index
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (addedNow) MutedText else PrimaryText,
                    )
                }
            }

            GlassActionRow(modifier = Modifier.fillMaxWidth()) {
                IconLabelButton(
                    text = "添加白名单",
                    icon = Icons.Filled.CheckCircle,
                    onClick = { pendingAllow = true },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    containerColor = MistGreenContainer,
                    contentColor = MistGreen,
                )
                IconLabelButton(
                    text = "添加黑名单",
                    icon = Icons.Filled.Block,
                    onClick = { pendingAllow = false },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    containerColor = MistRedContainer,
                    contentColor = MistRed,
                )
            }
        }
    }
}

@Composable
private fun SourceNotificationCard(record: NotificationRecord) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Notifications, contentDescription = null, tint = SecondaryText)
                Text(
                    text = record.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
            }
            Text(text = record.title, style = MaterialTheme.typography.titleSmall, color = PrimaryText)
            Text(text = record.body, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
            StatusPill(
                text = "正在标记关键词",
                icon = Icons.Filled.Edit,
                containerColor = Color.White.copy(alpha = 0.88f),
                contentColor = SecondaryText,
            )
        }
    }
}
