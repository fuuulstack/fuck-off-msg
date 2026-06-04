package com.hugo.notificationsilencer.ui.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.hugo.notificationsilencer.ui.components.PageHeader
import com.hugo.notificationsilencer.ui.components.StatItem
import com.hugo.notificationsilencer.ui.components.rememberAppLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val todayRecords = remember(records) { records.filter { it.isToday() } }
    val filteredRecords = remember(todayRecords, query) { todayRecords.filter { it.matchesQuery(query) } }
    val blockedCount = filteredRecords.count { it.decision == NotificationDecision.Blocked }
    val allowedCount = filteredRecords.size - blockedCount

    GlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                        onClick = onClearHistory,
                        enabled = records.isNotEmpty(),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CleaningServices,
                            contentDescription = "清空历史",
                            tint = if (records.isNotEmpty()) SecondaryText else SecondaryText.copy(alpha = 0.35f),
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
                    onValueChange = { query = it },
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
                        value = filteredRecords.size.toString(),
                        icon = Icons.Filled.Notifications,
                        modifier = Modifier.weight(1f),
                    )
                    StatItem(
                        label = "拦截",
                        value = blockedCount.toString(),
                        icon = Icons.Filled.Block,
                        modifier = Modifier.weight(1f),
                    )
                    StatItem(
                        label = "放行",
                        value = allowedCount.toString(),
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (filteredRecords.isEmpty()) {
                item { EmptyHistoryCard() }
            } else {
                items(filteredRecords, key = { it.id }) { record ->
                    SwipeRevealHistoryCard(
                        record = record,
                        onMark = { onMark(record) },
                        onDelete = { onDelete(record) },
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

@Composable
fun SwipeRevealHistoryCard(
    record: NotificationRecord,
    onMark: () -> Unit,
    onDelete: () -> Unit,
) {
    val actionWidth = 138.dp
    val quickSettleDistance = 24.dp
    val density = LocalDensity.current
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val quickSettleDistancePx = with(density) { quickSettleDistance.toPx() }
    val animatedOffset = remember(record.id) { Animatable(0f) }
    var dragOffset by remember(record.id) { mutableStateOf(0f) }
    var useDragOffset by remember(record.id) { mutableStateOf(false) }
    var revealed by remember(record.id) { mutableStateOf(false) }
    var settleStart by remember(record.id) { mutableStateOf(0f) }
    var settleTarget by remember(record.id) { mutableStateOf(0f) }
    var settleRequest by remember(record.id) { mutableStateOf(0) }
    val displayOffset = if (useDragOffset) dragOffset else animatedOffset.value

    LaunchedEffect(settleRequest) {
        if (settleRequest == 0) return@LaunchedEffect
        animatedOffset.snapTo(settleStart)
        useDragOffset = false
        animatedOffset.animateTo(
            targetValue = settleTarget,
            animationSpec = tween(durationMillis = 180),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .pointerInput(record.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    val startedRevealed = revealed
                    dragOffset = if (revealed) -actionWidthPx else 0f
                    useDragOffset = true

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        val dragAmount = change.positionChange().x
                        if (dragAmount != 0f) {
                            dragOffset = (dragOffset + dragAmount).coerceIn(-actionWidthPx, 0f)
                            change.consume()
                        }
                    } while (event.changes.any { !it.changedToUpIgnoreConsumed() })

                    revealed = if (startedRevealed) {
                        dragOffset < -actionWidthPx + quickSettleDistancePx
                    } else {
                        dragOffset < -quickSettleDistancePx
                    }
                    settleStart = dragOffset
                    settleTarget = if (revealed) -actionWidthPx else 0f
                    settleRequest += 1
                }
            },
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = displayOffset
            },
        ) {
            HistoryCard(record = record)
        }

        Row(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = actionWidthPx + displayOffset
                }
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HistorySwipeActionButton(
                icon = Icons.Filled.Sell,
                contentDescription = "标记",
                onClick = onMark,
            )
            HistorySwipeActionButton(
                icon = Icons.Filled.Delete,
                contentDescription = "删除",
                onClick = onDelete,
            )
        }
    }
}
@Composable
private fun HistorySwipeActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(58.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.74f)),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = SecondaryText,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun HistoryCard(record: NotificationRecord) {
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
            AppAvatar(appName = appLabel, packageName = record.packageName, modifier = Modifier.size(42.dp))
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

private fun NotificationRecord.matchesQuery(query: String): Boolean {
    val normalized = query.trim()
    if (normalized.isEmpty()) return true
    return listOf(appName, packageName, title, body, matchedKeyword.orEmpty())
        .any { it.contains(normalized, ignoreCase = true) }
}
