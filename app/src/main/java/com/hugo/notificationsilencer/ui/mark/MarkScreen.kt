package com.hugo.notificationsilencer.ui.mark

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.hugo.notificationsilencer.ui.components.PageHeader
import com.hugo.notificationsilencer.ui.components.StatusPill

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MarkScreen(
    record: NotificationRecord,
    onBack: () -> Unit,
    onAddWhitelist: (List<String>, RuleScope) -> Unit,
    onAddBlacklist: (List<String>, RuleScope) -> Unit,
) {
    BackHandler(onBack = onBack)

    val cells = remember(record.id) { TextCell.tokenize("${record.title}${record.body}") }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var added by remember { mutableStateOf(setOf<Int>()) }
    var pendingAllow by remember { mutableStateOf<Boolean?>(null) }
    val cellBounds = remember(record.id) { mutableStateMapOf<Int, Rect>() }

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
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回历史列表",
                        tint = SecondaryText,
                    )
                }
                PageHeader(
                    title = "涂抹选词",
                    subtitle = "连续选择视为一个词，非连续选择视为多个词",
                    icon = Icons.Filled.Edit,
                    modifier = Modifier.weight(1f),
                )
            }

            SourceNotificationCard(record = record)

            FlowRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(cells, added) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val touched = mutableSetOf<Int>()
                            val selectedBeforeGesture = selected
                            var moved = false

                            fun cellAt(position: Offset): Int? {
                                return cellBounds.entries.firstOrNull { (_, bounds) ->
                                    bounds.contains(position)
                                }?.key
                            }

                            fun smearAt(position: Offset) {
                                val index = cellAt(position) ?: return
                                if (added.contains(index) || touched.contains(index)) return
                                touched += index
                                selected = if (selectedBeforeGesture.contains(index)) {
                                    selected - index
                                } else {
                                    selected + index
                                }
                            }

                            smearAt(down.position)

                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                val delta = change.positionChange()
                                if (delta.x != 0f || delta.y != 0f) {
                                    moved = true
                                    smearAt(change.position)
                                    change.consume()
                                }
                            } while (event.changes.any { !it.changedToUpIgnoreConsumed() })

                            if (!moved && touched.size == 1) {
                                // The down event already applied the same toggle as a tap.
                            }
                        }
                    },
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
                            .onGloballyPositioned { coordinates ->
                                val parent = coordinates.parentLayoutCoordinates ?: return@onGloballyPositioned
                                val topLeft = parent.localPositionOf(coordinates, Offset.Zero)
                                cellBounds[cell.index] = Rect(
                                    left = topLeft.x,
                                    top = topLeft.y,
                                    right = topLeft.x + coordinates.size.width,
                                    bottom = topLeft.y + coordinates.size.height,
                                )
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
                IconButton(
                    onClick = { selected = emptySet() },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CleaningServices,
                        contentDescription = "清除勾选",
                        tint = if (selected.isNotEmpty()) SecondaryText else MutedText,
                    )
                }
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
