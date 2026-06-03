package com.hugo.notificationsilencer.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf

class PersistentSilencerRepository(
    private val context: Context,
) : SilencerRepository {
    private val historyRecords = mutableStateListOf<NotificationRecord>()
    private val ruleItems = mutableStateListOf<RuleItem>()

    init {
        val storedHistory = SilencerStore.loadHistory(context)
        val storedRules = SilencerStore.loadRules(context)
        historyRecords += storedHistory.ifEmpty { InMemorySilencerRepository.sample().history() }
        ruleItems += storedRules.ifEmpty { InMemorySilencerRepository.sample().rules() }
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

    override fun addKeywords(keywords: List<String>, allow: Boolean, scope: RuleScope) {
        val nextId = (ruleItems.maxOfOrNull { it.id } ?: 0L) + 1L
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
                )
            }
        ruleItems += newRules
        SilencerStore.saveRules(context, ruleItems)
    }
}
