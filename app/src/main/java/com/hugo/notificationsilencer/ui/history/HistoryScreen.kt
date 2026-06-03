package com.hugo.notificationsilencer.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.NotificationDecision
import com.hugo.notificationsilencer.data.NotificationRecord

@Composable
fun HistoryScreen(
    records: List<NotificationRecord>,
    onMark: (NotificationRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "历史",
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        items(records, key = { it.id }) { record ->
            HistoryCard(record = record, onMark = { onMark(record) })
        }
    }
}

@Composable
private fun HistoryCard(record: NotificationRecord, onMark: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = record.appName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = record.receivedAt, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = record.title, style = MaterialTheme.typography.titleSmall)
            Text(text = record.body, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(record.badgeText()) })
                Button(onClick = onMark) {
                    Text("标记")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = {}) {
                    Text("更多")
                }
            }
        }
    }
}

private fun NotificationRecord.badgeText(): String {
    return when (decision) {
        NotificationDecision.Allowed -> "已放行"
        NotificationDecision.Blocked -> "已拦截 · 命中：${matchedKeyword ?: "未知"}"
        NotificationDecision.SystemAllowed -> "系统放行"
        NotificationDecision.WhitelistAllowed -> "白名单放行 · ${matchedKeyword ?: "规则"}"
    }
}
