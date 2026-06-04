package com.hugo.notificationsilencer.ui.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.NotificationDecision
import com.hugo.notificationsilencer.data.NotificationRecord
import com.hugo.notificationsilencer.theme.MistRed
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText
import com.hugo.notificationsilencer.ui.components.AppAvatar
import com.hugo.notificationsilencer.ui.components.GlassBackground
import com.hugo.notificationsilencer.ui.components.GlassCard
import com.hugo.notificationsilencer.ui.components.IconLabelButton
import com.hugo.notificationsilencer.ui.components.PageHeader
import com.hugo.notificationsilencer.ui.components.StatItem
import com.hugo.notificationsilencer.ui.components.rememberAppLabel
import com.hugo.notificationsilencer.ui.gestures.DragIntent
import com.hugo.notificationsilencer.ui.gestures.dragIntent
import com.hugo.notificationsilencer.ui.selection.SelectionActionRow
import com.hugo.notificationsilencer.ui.selection.selectAllIds
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun HistoryScreen(
    records: List<NotificationRecord>,
    onMark: (NotificationRecord) -> Unit,
    onDelete: (NotificationRecord) -> Unit,
    onClearHistory: () -> Unit,
    notificationAccessGranted: Boolean,
    notificationListenerConnected: Boolean,
    onOpenNotificationAccessSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var decisionFilter by remember { mutableStateOf(HistoryDecisionFilter.All) }
    var activeActionRecordId by remember { mutableStateOf<Long?>(null) }
    var selectedRecordIds by remember { mutableStateOf(setOf<Long>()) }
    val todayRecords = remember(records) { records.filter { it.isToday() } }
    val queryMatchedRecords = remember(todayRecords, query) {
        filterHistoryRecords(todayRecords, query, HistoryDecisionFilter.All)
    }
    val filteredRecords = remember(queryMatchedRecords, decisionFilter) {
        filterHistoryRecords(queryMatchedRecords, query = "", decisionFilter = decisionFilter)
    }
    val blockedCount = queryMatchedRecords.count { it.decision == NotificationDecision.Blocked }
    val allowedCount = queryMatchedRecords.size - blockedCount
    val visibleRecordIds = remember(filteredRecords) { filteredRecords.map { it.id } }
    val selectedVisibleRecordIds = selectedRecordIds.intersect(visibleRecordIds.toSet())
    val selectionMode = selectedRecordIds.isNotEmpty()

    LaunchedEffect(records) {
        val existingIds = records.map { it.id }.toSet()
        selectedRecordIds = selectedRecordIds.intersect(existingIds)
    }

    GlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .dismissHistoryActionsOnBlankTap(
                    actionsVisible = activeActionRecordId != null,
                    onDismiss = { activeActionRecordId = null },
                )
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PageHeader(
                        title = "历史",
                        subtitle = "默认展示今天的推送，历史仅保留 7 天",
                        icon = Icons.Filled.History,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            activeActionRecordId = null
                            onClearHistory()
                        },
                        enabled = records.isNotEmpty(),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "清空历史",
                            tint = if (records.isNotEmpty()) MistRed else MistRed.copy(alpha = 0.35f),
                        )
                    }
                }
            }
            if (!notificationAccessGranted || !notificationListenerConnected) {
                item {
                    NotificationAccessCard(
                        notificationAccessGranted = notificationAccessGranted,
                        notificationListenerConnected = notificationListenerConnected,
                        onOpenSettings = onOpenNotificationAccessSettings,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        activeActionRecordId = null
                        selectedRecordIds = emptySet()
                        query = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(
                        label = "通知",
                        value = queryMatchedRecords.size.toString(),
                        icon = Icons.Filled.Notifications,
                        modifier = Modifier.weight(1f),
                        selected = decisionFilter == HistoryDecisionFilter.All,
                        onClick = {
                            activeActionRecordId = null
                            selectedRecordIds = emptySet()
                            decisionFilter = HistoryDecisionFilter.All
                        },
                    )
                    StatItem(
                        label = "拦截",
                        value = blockedCount.toString(),
                        icon = Icons.Filled.Block,
                        modifier = Modifier.weight(1f),
                        selected = decisionFilter == HistoryDecisionFilter.Blocked,
                        onClick = {
                            activeActionRecordId = null
                            selectedRecordIds = emptySet()
                            decisionFilter = HistoryDecisionFilter.Blocked
                        },
                    )
                    StatItem(
                        label = "放行",
                        value = allowedCount.toString(),
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.weight(1f),
                        selected = decisionFilter == HistoryDecisionFilter.Allowed,
                        onClick = {
                            activeActionRecordId = null
                            selectedRecordIds = emptySet()
                            decisionFilter = HistoryDecisionFilter.Allowed
                        },
                    )
                }
            }
            if (filteredRecords.isEmpty()) {
                item { EmptyHistoryCard() }
            } else {
                if (selectionMode) {
                    item {
                        SelectionActionRow(
                            selectedCount = selectedVisibleRecordIds.size,
                            allSelected = visibleRecordIds.isNotEmpty() && selectedVisibleRecordIds.size == visibleRecordIds.size,
                            onSelectAllChange = { checked ->
                                selectedRecordIds = if (checked) selectAllIds(visibleRecordIds) else emptySet()
                            },
                            onDeleteSelected = {
                                val idsToDelete = selectedVisibleRecordIds
                                activeActionRecordId = null
                                selectedRecordIds = emptySet()
                                filteredRecords
                                    .filter { idsToDelete.contains(it.id) }
                                    .forEach(onDelete)
                            },
                        )
                    }
                }
                items(filteredRecords, key = { it.id }) { record ->
                    SwipeRevealHistoryCard(
                        record = record,
                        actionsVisible = activeActionRecordId == record.id,
                        selectionMode = selectionMode,
                        selected = selectedRecordIds.contains(record.id),
                        onShowActions = { activeActionRecordId = record.id },
                        onDismissActions = { activeActionRecordId = null },
                        onLongPress = {
                            activeActionRecordId = null
                            selectedRecordIds = selectedRecordIds + record.id
                        },
                        onToggleSelection = {
                            selectedRecordIds = if (selectedRecordIds.contains(record.id)) {
                                selectedRecordIds - record.id
                            } else {
                                selectedRecordIds + record.id
                            }
                        },
                        onMark = { onMark(record) },
                        onDelete = {
                            activeActionRecordId = null
                            selectedRecordIds = selectedRecordIds - record.id
                            onDelete(record)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationAccessCard(
    notificationAccessGranted: Boolean,
    notificationListenerConnected: Boolean,
    onOpenSettings: () -> Unit,
) {
    val title = when {
        !notificationAccessGranted -> "通知监听未授权"
        !notificationListenerConnected -> "通知监听未连接"
        else -> "通知监听正常"
    }
    val body = when {
        !notificationAccessGranted -> "开启后才能记录和拦截新通知。"
        !notificationListenerConnected -> "系统授权已开启，但监听服务没有绑定。进入设置页把权限关开一次可恢复。"
        else -> "可以记录和拦截新通知。"
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsOff,
                contentDescription = null,
                tint = MistRed,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
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
            Button(onClick = onOpenSettings) {
                Text(if (notificationAccessGranted) "重新授权" else "去授权")
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "还没有通知记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
            )
            Text(
                text = "授权通知监听后，这里会显示所有 App 的通知历史。",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeRevealHistoryCard(
    record: NotificationRecord,
    actionsVisible: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    onShowActions: () -> Unit,
    onDismissActions: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelection: () -> Unit,
    onMark: () -> Unit,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val deleteThresholdPx = with(density) { 80.dp.toPx() }
    val deleteExitPx = with(density) { 420.dp.toPx() }
    val deleteOffset = remember(record.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var deleting by remember(record.id) { mutableStateOf(false) }

    fun animateSwipeDelete(direction: Float) {
        if (deleting) return
        deleting = true
        scope.launch {
            deleteOffset.animateTo(
                targetValue = direction * deleteExitPx,
                animationSpec = tween(durationMillis = 180),
            )
            onDelete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .combinedClickable(
                onClick = {
                    if (selectionMode) onToggleSelection() else onShowActions()
                },
                onLongClick = onLongPress,
            )
            .then(
                if (selectionMode) {
                    Modifier
                } else {
                    Modifier.pointerInput(record.id) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            var totalX = 0f
                            var totalY = 0f
                            var intent = DragIntent.Undecided

                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                val delta = change.positionChange()
                                totalX += delta.x
                                totalY += delta.y
                                if (intent == DragIntent.Undecided) {
                                    intent = dragIntent(totalX, totalY, viewConfiguration.touchSlop)
                                }
                                if (intent == DragIntent.HorizontalAction && delta.x != 0f) {
                                    change.consume()
                                }
                            } while (event.changes.any { !it.changedToUpIgnoreConsumed() })

                            if (intent == DragIntent.VerticalScroll) {
                                return@awaitEachGesture
                            }

                            when (historySwipeAction(totalX, deleteThresholdPx)) {
                                HistorySwipeAction.Delete -> animateSwipeDelete(if (totalX < 0f) -1f else 1f)
                                HistorySwipeAction.None -> Unit
                            }
                        }
                    }
                },
            ),
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = deleteOffset.value
                alpha = (1f - abs(deleteOffset.value) / deleteExitPx).coerceIn(0f, 1f)
            },
        ) {
            HistoryCard(record = record, selectionMode = selectionMode, selected = selected)

            if (actionsVisible && !selectionMode) {
                HistoryActionOverlay(
                    onDismiss = onDismissActions,
                    onMark = {
                        onDismissActions()
                        onMark()
                    },
                    onDelete = {
                        onDismissActions()
                        onDelete()
                    },
                )
            }
        }
    }
}

fun Modifier.dismissHistoryActionsOnBlankTap(
    actionsVisible: Boolean,
    onDismiss: () -> Unit,
): Modifier {
    if (!actionsVisible) return this
    return pointerInput(actionsVisible) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = true)
            var moved = false
            do {
                val event = awaitPointerEvent()
                event.changes.forEach { change ->
                    val delta = change.positionChange()
                    if (delta.x != 0f || delta.y != 0f) {
                        moved = true
                    }
                }
            } while (event.changes.any { !it.changedToUpIgnoreConsumed() })

            if (!moved) {
                onDismiss()
            }
        }
    }
}

@Composable
private fun BoxScope.HistoryActionOverlay(
    onDismiss: () -> Unit,
    onMark: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconLabelButton(
                text = "标记",
                icon = Icons.Filled.Edit,
                onClick = onMark,
                containerColor = Color.White,
                contentColor = PrimaryText,
            )
            IconLabelButton(
                text = "删除",
                icon = Icons.Filled.Delete,
                onClick = onDelete,
                containerColor = MistRed,
                contentColor = Color.White,
            )
        }
    }
}

@Composable
private fun HistoryCard(
    record: NotificationRecord,
    selectionMode: Boolean,
    selected: Boolean,
) {
    val appLabel = rememberAppLabel(record.packageName, record.appName)
    val snippet = notificationSnippet(
        title = record.title,
        body = record.body,
        keyword = record.matchedKeyword.takeIf { record.decision == NotificationDecision.Blocked },
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryText,
                        uncheckedColor = SecondaryText,
                        checkmarkColor = Color.White,
                    ),
                )
            }
            AppAvatar(
                appName = appLabel,
                packageName = record.packageName,
                modifier = Modifier.size(42.dp),
                showContainer = false,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appLabel,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = record.displayTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText,
                        maxLines = 1,
                    )
                }
                Text(
                    text = snippet.asAnnotatedText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NotificationSnippet.asAnnotatedText() = buildAnnotatedString {
    if (highlightStart < 0 || highlightEnd <= highlightStart) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, highlightStart))
    withStyle(SpanStyle(color = MistRed, fontWeight = FontWeight.SemiBold)) {
        append(text.substring(highlightStart, highlightEnd))
    }
    append(text.substring(highlightEnd))
}

private fun NotificationRecord.displayTime(): String {
    val millis = receivedAt.toLongOrNull() ?: return receivedAt
    return DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))
}

private fun NotificationRecord.isToday(): Boolean {
    val millis = receivedAt.toLongOrNull() ?: return false
    val zoneId = ZoneId.systemDefault()
    val recordDate = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
    return recordDate == LocalDate.now(zoneId)
}

