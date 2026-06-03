package com.hugo.notificationsilencer.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.rules.DefaultKeywords

@Composable
fun RulesScreen(rules: List<RuleItem>, modifier: Modifier = Modifier) {
    var enhancedEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(text = "规则", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "默认保守规则", style = MaterialTheme.typography.titleMedium)
                    Text(text = DefaultKeywords.Conservative.joinToString("、"))
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "增强营销规则", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = enhancedEnabled,
                        onCheckedChange = { enhancedEnabled = it },
                    )
                    Text(text = "开启后误杀风险更高")
                }
            }
        }
        items(rules, key = { it.id }) { rule ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = if (rule.allow) "白名单" else "黑名单")
                    Text(text = "${rule.keyword} · ${rule.scope}")
                }
            }
        }
    }
}
