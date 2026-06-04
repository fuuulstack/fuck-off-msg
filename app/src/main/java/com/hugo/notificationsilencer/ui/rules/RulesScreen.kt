package com.hugo.notificationsilencer.ui.rules

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.hugo.notificationsilencer.theme.SoftGray
import com.hugo.notificationsilencer.ui.components.AppAvatar
import com.hugo.notificationsilencer.ui.components.GlassBackground
import com.hugo.notificationsilencer.ui.components.GlassCard
import com.hugo.notificationsilencer.ui.components.PageHeader
import com.hugo.notificationsilencer.ui.components.StatusPill
import com.hugo.notificationsilencer.ui.selection.SelectionActionRow
import com.hugo.notificationsilencer.ui.selection.selectAllIds

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(
    rules: List<RuleItem>,
    enhancedMarketingRulesEnabled: Boolean,
    onEnhancedMarketingRulesEnabledChange: (Boolean) -> Unit,
    onDeleteRules: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedRuleIds by remember { mutableStateOf(setOf<Long>()) }
    val visibleRuleIds = remember(rules) { rules.map { it.id } }
    val selectedVisibleRuleIds = selectedRuleIds.intersect(visibleRuleIds.toSet())
    val selectionMode = selectedRuleIds.isNotEmpty()

    LaunchedEffect(rules) {
        val existingIds = rules.map { it.id }.toSet()
        selectedRuleIds = selectedRuleIds.intersect(existingIds)
    }

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
                                text = "严格模式",
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
                            checked = enhancedMarketingRulesEnabled,
                            onCheckedChange = onEnhancedMarketingRulesEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryText,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = SoftGray,
                                uncheckedBorderColor = Color.Transparent,
                            ),
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
                if (selectionMode) {
                    item {
                        SelectionActionRow(
                            selectedCount = selectedVisibleRuleIds.size,
                            allSelected = visibleRuleIds.isNotEmpty() && selectedVisibleRuleIds.size == visibleRuleIds.size,
                            onSelectAllChange = { checked ->
                                selectedRuleIds = if (checked) selectAllIds(visibleRuleIds) else emptySet()
                            },
                            onDeleteSelected = {
                                val idsToDelete = selectedVisibleRuleIds
                                selectedRuleIds = emptySet()
                                onDeleteRules(idsToDelete)
                            },
                        )
                    }
                }
                items(rules, key = { it.id }) { rule ->
                    RuleCard(
                        rule = rule,
                        selectionMode = selectionMode,
                        selected = selectedRuleIds.contains(rule.id),
                        onLongPress = { selectedRuleIds = selectedRuleIds + rule.id },
                        onToggleSelection = {
                            selectedRuleIds = if (selectedRuleIds.contains(rule.id)) {
                                selectedRuleIds - rule.id
                            } else {
                                selectedRuleIds + rule.id
                            }
                        },
                    )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RuleCard(
    rule: RuleItem,
    selectionMode: Boolean,
    selected: Boolean,
    onLongPress: () -> Unit,
    onToggleSelection: () -> Unit,
) {
    val label = if (rule.allow) "白名单" else "黑名单"
    val icon = if (rule.allow) Icons.Filled.CheckCircle else Icons.Filled.Block
    val container = if (rule.allow) MistGreenContainer else MistRedContainer
    val content = if (rule.allow) MistGreen else MistRed
    val scopePresentation = rule.scopePresentation()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (selectionMode) onToggleSelection()
                },
                onLongClick = onLongPress,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
            StatusPill(text = label, icon = icon, containerColor = container, contentColor = content)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = rule.keyword,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
                RuleScopeLine(scopePresentation = scopePresentation)
            }
        }
    }
}

@Composable
private fun RuleScopeLine(scopePresentation: RuleScopePresentation) {
    when (scopePresentation) {
        RuleScopePresentation.Global -> {
            Text(
                text = RuleScope.Global.scopeLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
            )
        }
        is RuleScopePresentation.App -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppAvatar(
                    appName = scopePresentation.appName,
                    packageName = scopePresentation.packageName,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = scopePresentation.appName,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        RuleScopePresentation.UnknownApp -> {
            Text(
                text = "App 专属规则",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
            )
        }
    }
}

private fun RuleScope.scopeLabel(): String {
    return when (this) {
        RuleScope.Global -> "全局规则"
        RuleScope.CurrentApp -> "仅当前 App"
    }
}
