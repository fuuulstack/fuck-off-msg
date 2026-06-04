package com.hugo.notificationsilencer.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PersistentSilencerRepository(
    private val context: Context,
) : SilencerRepository {
    private val historyRecords = mutableStateListOf<NotificationRecord>()
    private val ruleItems = mutableStateListOf<RuleItem>()
    private var settings by mutableStateOf(SilencerSettings())
    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (SilencerStore.isHistoryKey(key)) {
            mainHandler.post {
                historyRecords.clear()
                historyRecords += SilencerStore.loadHistory(context)
            }
        }
    }

    init {
        val storedHistory = SilencerStore.loadHistory(context)
        val storedRules = SilencerStore.loadRules(context)
        settings = SilencerStore.loadSettings(context)
        historyRecords += storedHistory
        ruleItems += storedRules
        SilencerStore.registerHistoryListener(context, preferenceListener)
    }

    override fun history(): List<NotificationRecord> {
        return historyRecords.sortedByDescending { it.id }
    }

    override fun appSummaries(): List<AppSummary> {
        return historyRecords
            .groupBy { it.packageName }
            .map { (packageName, records) ->
                AppSummary(
                    packageName = packageName,
                    appName = records.first().appName,
                    totalCount = records.size,
                    blockedCount = records.count { it.decision == NotificationDecision.Blocked },
                    recentKeyword = records.firstNotNullOfOrNull { it.matchedKeyword },
                )
            }
            .sortedByDescending { it.totalCount }
    }

    override fun rules(): List<RuleItem> {
        return ruleItems.toList()
    }

    override fun settings(): SilencerSettings {
        return settings
    }

    override fun setEnhancedMarketingRulesEnabled(enabled: Boolean) {
        settings = settings.copy(enhancedMarketingRulesEnabled = enabled)
        SilencerStore.saveSettings(context, settings)
    }

    override fun addKeywords(
        keywords: List<String>,
        allow: Boolean,
        scope: RuleScope,
        packageName: String?,
        appName: String?,
    ) {
        val nextId = (ruleItems.maxOfOrNull { it.id } ?: 0L) + 1L
        val scopedPackageName = packageName.takeIf { scope == RuleScope.CurrentApp }
        val scopedAppName = appName.takeIf { scope == RuleScope.CurrentApp }
        val newRules = keywords
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .mapIndexed { index, keyword ->
                RuleItem(
                    id = nextId + index,
                    keyword = keyword,
                    allow = allow,
                    scope = scope,
                    packageName = scopedPackageName,
                    appName = scopedAppName,
                )
            }
        ruleItems += newRules
        SilencerStore.saveRules(context, ruleItems)
    }

    override fun deleteRules(ids: Set<Long>) {
        if (ids.isEmpty()) return
        ruleItems.removeAll { ids.contains(it.id) }
        SilencerStore.saveRules(context, ruleItems)
    }

    override fun deleteHistoryRecord(id: Long) {
        historyRecords.removeAll { it.id == id }
        SilencerStore.saveHistory(context, historyRecords)
    }

    override fun clearHistory() {
        historyRecords.clear()
        SilencerStore.saveHistory(context, emptyList())
    }
}
