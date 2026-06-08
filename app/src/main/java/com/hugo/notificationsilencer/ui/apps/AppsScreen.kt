package com.hugo.notificationsilencer.ui.apps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.data.AppSummary
import com.hugo.notificationsilencer.data.NotificationRecord
import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.data.isAppWideBlockRule
import com.hugo.notificationsilencer.theme.MistBlue
import com.hugo.notificationsilencer.theme.MistBlueContainer
import com.hugo.notificationsilencer.theme.MistRed
import com.hugo.notificationsilencer.theme.MistRedContainer
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText
import com.hugo.notificationsilencer.ui.components.AppAvatar
import com.hugo.notificationsilencer.ui.components.GlassBackground
import com.hugo.notificationsilencer.ui.components.GlassCard
import com.hugo.notificationsilencer.ui.components.PageHeader
import com.hugo.notificationsilencer.ui.components.StatusPill
import com.hugo.notificationsilencer.ui.history.SwipeRevealHistoryCard
import com.hugo.notificationsilencer.ui.history.dismissHistoryActionsOnBlankTap
import com.hugo.notificationsilencer.ui.i18n.LocalSilencerStrings
import com.hugo.notificationsilencer.ui.selection.SelectionActionRow
import com.hugo.notificationsilencer.ui.selection.selectAllIds
import kotlinx.coroutines.launch

@Composable
fun AppsScreen(
    summaries: List<AppSummary>,
    records: List<NotificationRecord>,
    rules: List<RuleItem>,
    onMark: (NotificationRecord) -> Unit,
    onDelete: (NotificationRecord) -> Unit,
    onToggleAppBlock: (AppSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalSilencerStrings.current
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    val selectedSummary = summaries.firstOrNull { it.packageName == selectedPackage }

    if (selectedPackage != null) {
        BackHandler {
            selectedPackage = null
        }
        AppNotificationsScreen(
            summary = selectedSummary,
            packageName = selectedPackage.orEmpty(),
            records = records.filter { it.packageName == selectedPackage },
            onMark = onMark,
            onDelete = onDelete,
            onBack = { selectedPackage = null },
            modifier = modifier,
        )
        return
    }

    AppsListScreen(
        summaries = summaries,
        rules = rules,
        onOpenApp = { selectedPackage = it.packageName },
        onToggleAppBlock = onToggleAppBlock,
        modifier = modifier,
    )
}

@Composable
private fun AppsListScreen(
    summaries: List<AppSummary>,
    rules: List<RuleItem>,
    onOpenApp: (AppSummary) -> Unit,
    onToggleAppBlock: (AppSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalSilencerStrings.current
    var query by remember { mutableStateOf("") }
    val filteredSummaries = remember(summaries, query) {
        summaries.filter { it.matchesQuery(query) }
    }
    val groupedSummaries = remember(filteredSummaries) {
        filteredSummaries.groupBy { it.indexLetter() }
            .toSortedMap(compareBy { if (it == "#") "ZZZ" else it })
    }
    val listState = rememberLazyListState()
    val blockedPackages = remember(rules) {
        rules
            .filter { it.isAppWideBlockRule() }
            .mapNotNull { it.packageName }
            .toSet()
    }
    val indexPositions = remember(groupedSummaries) {
        val positions = mutableMapOf<String, Int>()
        var index = 2
        groupedSummaries.forEach { (letter, apps) ->
            positions[letter] = index
            index += 1 + apps.size
        }
        positions
    }

    GlassBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    PageHeader(
                        title = strings.apps,
                        subtitle = strings.appsSubtitle,
                        icon = Icons.Filled.Apps,
                    )
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
                            focusedLeadingIconColor = SecondaryText,
                            unfocusedLeadingIconColor = SecondaryText,
                        ),
                    )
                }
                if (filteredSummaries.isEmpty()) {
                    item { EmptyAppsCard(query = query) }
                } else {
                    groupedSummaries.forEach { (letter, apps) ->
                        item(key = "header-$letter") {
                            Text(
                                text = letter,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryText,
                            )
                        }
                        items(apps, key = { it.packageName }) { summary ->
                            AppSummaryCard(
                                summary = summary,
                                appBlocked = blockedPackages.contains(summary.packageName),
                                onClick = { onOpenApp(summary) },
                                onToggleAppBlock = { onToggleAppBlock(summary) },
                            )
                        }
                    }
                }
            }

            if (groupedSummaries.size > 1) {
                AlphabetIndex(
                    letters = groupedSummaries.keys.toList(),
                    listState = listState,
                    indexPositions = indexPositions,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }
}

@Composable
private fun AppSummaryCard(
    summary: AppSummary,
    appBlocked: Boolean,
    onClick: () -> Unit,
    onToggleAppBlock: () -> Unit,
) {
    val strings = LocalSilencerStrings.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BlockableAppAvatar(
                summary = summary,
                blocked = appBlocked,
                onClick = onToggleAppBlock,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = summary.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        text = "${strings.notifications} ${summary.totalCount}",
                        icon = Icons.Filled.Notifications,
                        containerColor = MistBlueContainer,
                        contentColor = MistBlue,
                    )
                    StatusPill(
                        text = "${strings.blocked} ${summary.blockedCount}",
                        icon = Icons.Filled.Block,
                        containerColor = MistRedContainer,
                        contentColor = MistRed,
                    )
                }
                summary.recentKeyword?.let {
                    StatusPill(
                        text = "${strings.recentMatch}$it",
                        icon = Icons.Filled.Sell,
                        containerColor = MistRedContainer,
                        contentColor = MistRed,
                    )
                }
            }
            Spacer(modifier = Modifier.padding(2.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SecondaryText,
            )
        }
    }
}

@Composable
private fun BlockableAppAvatar(
    summary: AppSummary,
    blocked: Boolean,
    onClick: () -> Unit,
) {
    val strings = LocalSilencerStrings.current
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppAvatar(appName = summary.appName, packageName = summary.packageName)
        if (blocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.68f))
                    .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Block,
                    contentDescription = strings.appBlockedAll,
                    tint = MistRed,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun AlphabetIndex(
    letters: List<String>,
    listState: LazyListState,
    indexPositions: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(end = 4.dp)
            .width(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        letters.forEach { letter ->
            Text(
                text = letter,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable {
                        indexPositions[letter]?.let { index ->
                            scope.launch { listState.animateScrollToItem(index) }
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText,
            )
        }
    }
}

@Composable
private fun EmptyAppsCard(query: String) {
    val strings = LocalSilencerStrings.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (query.isBlank()) strings.noAppRecords else strings.noMatchedApps,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
        )
    }
}

@Composable
private fun AppNotificationsScreen(
    summary: AppSummary?,
    packageName: String,
    records: List<NotificationRecord>,
    onMark: (NotificationRecord) -> Unit,
    onDelete: (NotificationRecord) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalSilencerStrings.current
    val appName = summary?.appName ?: records.firstOrNull()?.appName ?: packageName
    var activeActionRecordId by remember { mutableStateOf<Long?>(null) }
    var selectedRecordIds by remember(packageName) { mutableStateOf(setOf<Long>()) }
    val visibleRecordIds = remember(records) { records.map { it.id } }
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            activeActionRecordId = null
                            selectedRecordIds = emptySet()
                            onBack()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backToApps,
                            tint = SecondaryText,
                        )
                    }
                    PageHeader(
                        title = appName,
                        subtitle = strings.appNotificationsSubtitle,
                        icon = Icons.Filled.Notifications,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (records.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = strings.appNoRecords,
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                        )
                    }
                }
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
                                records
                                    .filter { idsToDelete.contains(it.id) }
                                    .forEach(onDelete)
                            },
                        )
                    }
                }
                items(records, key = { it.id }) { record ->
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

private fun AppSummary.matchesQuery(query: String): Boolean {
    val normalized = query.trim()
    if (normalized.isEmpty()) return true
    return listOf(appName, packageName, recentKeyword.orEmpty())
        .any { it.contains(normalized, ignoreCase = true) }
}

private fun AppSummary.indexLetter(): String {
    val appInitial = appName.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' }
    val packageInitial = packageName.substringAfterLast('.').firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' }
    return (appInitial ?: packageInitial)?.toString() ?: "#"
}
