package com.hugo.notificationsilencer.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.data.RuleScope
import com.hugo.notificationsilencer.rules.DefaultKeywords
import com.hugo.notificationsilencer.theme.MistBlue
import com.hugo.notificationsilencer.theme.MistBlueContainer
import com.hugo.notificationsilencer.theme.MistGreen
import com.hugo.notificationsilencer.theme.MistGreenContainer
import com.hugo.notificationsilencer.theme.MistRed
import com.hugo.notificationsilencer.theme.MistRedContainer
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText
import com.hugo.notificationsilencer.ui.components.GlassBackground
import com.hugo.notificationsilencer.ui.components.GlassCard
import com.hugo.notificationsilencer.ui.components.PageHeader
import com.hugo.notificationsilencer.ui.components.StatusPill

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(rules: List<RuleItem>, modifier: Modifier = Modifier) {
    var enhancedEnabled by remember { mutableStateOf(false) }

    GlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = "规则",
                    subtitle = "系统级优先，白名单优先",
                    icon = Icons.AutoMirrored.Filled.Rule,
                )
            }
            item {
                PriorityCard()
            }
            item {
                KeywordCard(
                    title = "默认保守规则",
                    subtitle = "内置营销关键词，适合日常低误伤拦截",
                    icon = Icons.Filled.Shield,
                    keywords = DefaultKeywords.Conservative,
                )
            }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MistBlue,
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "增强营销规则",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryText,
                            )
                            Text(
                                text = "开启后拦截更激进，误伤风险也更高",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText,
                            )
                        }
                        Switch(
                            checked = enhancedEnabled,
                            onCheckedChange = { enhancedEnabled = it },
                        )
                    }
                }
            }
            item {
                SectionLabel(title = "我的规则", icon = Icons.Filled.Tune)
            }
            if (rules.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "还没有手动添加的规则。可以在历史通知里点击标记后涂抹关键词。",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                        )
                    }
                }
            } else {
                items(rules, key = { it.id }) { rule ->
                    RuleCard(rule = rule)
                }
            }
        }
    }
}

@Composable
private fun PriorityCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = MistBlue)
                Text(
                    text = "规则优先级",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(
                    text = "系统级优先",
                    icon = Icons.Filled.Security,
                    containerColor = MistBlueContainer,
                    contentColor = MistBlue,
                )
                StatusPill(
                    text = "白名单优先",
                    icon = Icons.Filled.CheckCircle,
                    containerColor = MistGreenContainer,
                    contentColor = MistGreen,
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun KeywordCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keywords: Collection<String>,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MistBlue)
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText,
                    )
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                keywords.forEach { keyword ->
                    StatusPill(
                        text = keyword,
                        icon = Icons.Filled.Shield,
                        containerColor = MistBlueContainer,
                        contentColor = MistBlue,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = SecondaryText)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
        )
    }
}

@Composable
private fun RuleCard(rule: RuleItem) {
    val label = if (rule.allow) "白名单" else "黑名单"
    val icon = if (rule.allow) Icons.Filled.CheckCircle else Icons.Filled.Block
    val container = if (rule.allow) MistGreenContainer else MistRedContainer
    val content = if (rule.allow) MistGreen else MistRed

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(text = label, icon = icon, containerColor = container, contentColor = content)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = rule.keyword,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
                Text(
                    text = rule.scope.scopeLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                )
            }
        }
    }
}

private fun RuleScope.scopeLabel(): String {
    return when (this) {
        RuleScope.Global -> "全局规则"
        RuleScope.CurrentApp -> "仅当前 App"
    }
}
