package com.hugo.notificationsilencer.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.AppSummary

@Composable
fun AppsScreen(summaries: List<AppSummary>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(text = "应用", style = MaterialTheme.typography.headlineMedium)
        }
        items(summaries, key = { it.packageName }) { summary ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = summary.appName, style = MaterialTheme.typography.titleMedium)
                    Text(text = "通知 ${summary.totalCount} 条 · 拦截 ${summary.blockedCount} 条")
                    summary.recentKeyword?.let {
                        Text(text = "最近命中：$it")
                    }
                }
            }
        }
    }
}
