package com.hugo.notificationsilencer.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    records: List<NotificationRecord>,
    onMark: (NotificationRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val blockedCount = records.count { it.decision == NotificationDecision.Blocked }
    val allowedCount = records.size - blockedCount

    GlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                PageHeader(
                    title = "历史",
                    subtitle = "默认平铺展示所有 App 的通知记录",
                    icon = Icons.Filled.History,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(
                        label = "通知",
                        value = records.size.toString(),
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
            if (records.isEmpty()) {
                item { EmptyHistoryCard() }
            } else {
                items(records, key = { it.id }) { record ->
                    SwipeRevealHistoryCard(record = record, onMark = { onMark(record) })
                }
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
private fun SwipeRevealHistoryCard(record: NotificationRecord, onMark: () -> Unit) {
    var dragDistance by remember(record.id) { mutableStateOf(0f) }
    var revealed by remember(record.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(record.id) {
                detectHorizontalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onDragEnd = {
                        when {
                            dragDistance < -80f -> revealed = true
                            dragDistance > 80f -> revealed = false
                        }
                        dragDistance = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragDistance += dragAmount
                    },
                )
            },
    ) {
        HistoryCard(record = record)

        if (revealed) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconLabelButton(
                    text = "标记",
                    icon = Icons.Filled.Edit,
                    onClick = onMark,
                    containerColor = PrimaryText,
                    contentColor = Color.White,
                )
            }
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

    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
