package com.hugo.notificationsilencer.ui.mark

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.NotificationRecord
import com.hugo.notificationsilencer.data.RuleScope

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
        AlertDialog(
            onDismissRequest = { pendingAllow = null },
            title = { Text(if (allow) "添加白名单" else "添加黑名单") },
            text = { Text(selectedKeywords().joinToString("、")) },
            confirmButton = {
                Button(
                    onClick = {
                        val keywords = selectedKeywords()
                        if (allow) onAddWhitelist(keywords, RuleScope.Global)
                        else onAddBlacklist(keywords, RuleScope.Global)
                        added = added + selected
                        selected = emptySet()
                        pendingAllow = null
                    },
                ) {
                    Text(if (allow) "添加为全局白名单" else "添加为全局黑名单")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val keywords = selectedKeywords()
                        if (allow) onAddWhitelist(keywords, RuleScope.CurrentApp)
                        else onAddBlacklist(keywords, RuleScope.CurrentApp)
                        added = added + selected
                        selected = emptySet()
                        pendingAllow = null
                    },
                ) {
                    Text(if (allow) "仅当前 App 放行" else "仅当前 App 拦截")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) {
                Text("返回")
            }
            Text(text = "涂抹选择文字", style = MaterialTheme.typography.headlineSmall)
        }
        Text(text = "${record.appName} · ${record.title}")
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cells.forEach { cell ->
                val selectedNow = selected.contains(cell.index)
                val addedNow = added.contains(cell.index)
                Text(
                    text = cell.text,
                    modifier = Modifier
                        .background(
                            when {
                                addedNow -> Color(0xFFE0E0E0)
                                selectedNow -> Color(0xFFB7E4C7)
                                else -> Color(0xFFF6F6F6)
                            },
                        )
                        .clickable(enabled = !addedNow) {
                            selected = if (selectedNow) selected - cell.index else selected + cell.index
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { pendingAllow = true },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text("添加白名单")
            }
            Button(
                onClick = { pendingAllow = false },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text("添加黑名单")
            }
        }
    }
}
