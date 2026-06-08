package com.hugo.notificationsilencer.ui.rules

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.hugo.notificationsilencer.data.APP_WIDE_BLOCK_LABEL
import com.hugo.notificationsilencer.data.AppLanguage
import com.hugo.notificationsilencer.data.AppSummary
import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.data.RuleScope
import com.hugo.notificationsilencer.data.isAppWideBlockRule
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
import com.hugo.notificationsilencer.ui.i18n.LocalSilencerStrings
import com.hugo.notificationsilencer.ui.selection.SelectionActionRow
import com.hugo.notificationsilencer.ui.selection.selectAllIds

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(
    rules: List<RuleItem>,
    appSummaries: List<AppSummary>,
    appLanguage: AppLanguage,
    enhancedMarketingRulesEnabled: Boolean,
    onEnhancedMarketingRulesEnabledChange: (Boolean) -> Unit,
    onAddRule: (
        keyword: String,
        allow: Boolean,
        scope: RuleScope,
        packageName: String?,
        appName: String?,
    ) -> Unit,
    onDeleteRules: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalSilencerStrings.current
    var selectedRuleIds by remember { mutableStateOf(setOf<Long>()) }
    var addingRule by remember { mutableStateOf(false) }
    val visibleRuleIds = remember(rules) { rules.map { it.id } }
    val selectedVisibleRuleIds = selectedRuleIds.intersect(visibleRuleIds.toSet())
    val selectionMode = selectedRuleIds.isNotEmpty()

    LaunchedEffect(rules) {
        val existingIds = rules.map { it.id }.toSet()
        selectedRuleIds = selectedRuleIds.intersect(existingIds)
    }

    if (addingRule) {
        AddRuleDialog(
            appSummaries = appSummaries,
            onDismiss = { addingRule = false },
            onConfirm = { keyword, allow, scope, packageName, appName ->
                onAddRule(keyword, allow, scope, packageName, appName)
                addingRule = false
            },
        )
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
                    title = strings.rules,
                    subtitle = strings.rulesSubtitle,
                    icon = Icons.AutoMirrored.Filled.Rule,
                )
            }
            item {
                PriorityCard()
            }
            item {
                KeywordCard(
                    title = strings.defaultConservativeRules,
                    subtitle = strings.defaultConservativeRulesSubtitle,
                    icon = Icons.Filled.Shield,
                    keywords = DefaultKeywords.conservativeFor(appLanguage),
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
                                text = strings.strictMode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryText,
                            )
                            Text(
                                text = strings.strictModeSubtitle,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel(
                        title = strings.myRules,
                        icon = Icons.Filled.Tune,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { addingRule = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = strings.addRule,
                            tint = PrimaryText,
                        )
                    }
                }
            }
            if (rules.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = strings.noManualRules,
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
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
private fun AddRuleDialog(
    appSummaries: List<AppSummary>,
    onDismiss: () -> Unit,
    onConfirm: (
        keyword: String,
        allow: Boolean,
        scope: RuleScope,
        packageName: String?,
        appName: String?,
    ) -> Unit,
) {
    val strings = LocalSilencerStrings.current
    var keyword by remember { mutableStateOf("") }
    var allow by remember { mutableStateOf(false) }
    var scope by remember { mutableStateOf(RuleScope.Global) }
    var selectedAppPackage by remember(appSummaries) { mutableStateOf(appSummaries.firstOrNull()?.packageName) }
    val selectedApp = appSummaries.firstOrNull { it.packageName == selectedAppPackage }
    val canConfirm = keyword.trim().isNotEmpty() && (scope == RuleScope.Global || selectedApp != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addRuleTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = !allow,
                        onClick = { allow = false },
                        label = { Text(strings.blacklist) },
                        leadingIcon = {
                            Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                    FilterChip(
                        selected = allow,
                        onClick = { allow = true },
                        label = { Text(strings.whitelist) },
                        leadingIcon = {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                    FilterChip(
                        selected = scope == RuleScope.Global,
                        onClick = { scope = RuleScope.Global },
                        label = { Text(strings.global) },
                    )
                    FilterChip(
                        selected = scope == RuleScope.CurrentApp,
                        onClick = {
                            scope = RuleScope.CurrentApp
                            if (selectedAppPackage == null) {
                                selectedAppPackage = appSummaries.firstOrNull()?.packageName
                            }
                        },
                        label = { Text(strings.specificApp) },
                    )
                }
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(strings.keyword) },
                )
                if (scope == RuleScope.CurrentApp) {
                    if (appSummaries.isEmpty()) {
                        Text(
                            text = strings.noSelectableApps,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(appSummaries, key = { it.packageName }) { app ->
                                AppRuleTargetRow(
                                    app = app,
                                    selected = selectedAppPackage == app.packageName,
                                    onClick = { selectedAppPackage = app.packageName },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        keyword.trim(),
                        allow,
                        scope,
                        selectedApp?.packageName.takeIf { scope == RuleScope.CurrentApp },
                        selectedApp?.appName.takeIf { scope == RuleScope.CurrentApp },
                    )
                },
                enabled = canConfirm,
            ) {
                Text(strings.add)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
    )
}

@Composable
private fun AppRuleTargetRow(
    app: AppSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(appName = app.appName, packageName = app.packageName, modifier = Modifier.size(28.dp))
            Text(
                text = app.appName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MistGreen)
            }
        }
    }
}

@Composable
private fun PriorityCard() {
    val strings = LocalSilencerStrings.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = MistBlue)
                Text(
                    text = strings.rulePriority,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(
                    text = strings.systemFirst,
                    icon = Icons.Filled.Security,
                    containerColor = MistBlueContainer,
                    contentColor = MistBlue,
                )
                StatusPill(
                    text = strings.whitelistFirst,
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
private fun SectionLabel(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(top = 4.dp),
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
    val strings = LocalSilencerStrings.current
    val label = if (rule.allow) strings.whitelist else strings.blacklist
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
                    text = if (rule.isAppWideBlockRule()) strings.allNotifications else rule.keyword,
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
    val strings = LocalSilencerStrings.current
    when (scopePresentation) {
        RuleScopePresentation.Global -> {
            Text(
                text = strings.globalRule,
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
                text = strings.appScopedRule,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
            )
        }
    }
}
