package com.hugo.notificationsilencer.data

enum class NotificationDecision {
    Allowed,
    Blocked,
    SystemAllowed,
    WhitelistAllowed,
}

enum class RuleScope {
    Global,
    CurrentApp,
}

data class NotificationRecord(
    val id: Long,
    val notificationKey: String? = null,
    val packageName: String,
    val appName: String,
    val title: String,
    val body: String,
    val receivedAt: String,
    val decision: NotificationDecision,
    val matchedKeyword: String? = null,
)

data class AppSummary(
    val packageName: String,
    val appName: String,
    val totalCount: Int,
    val blockedCount: Int,
    val recentKeyword: String?,
)

data class RuleItem(
    val id: Long,
    val keyword: String,
    val allow: Boolean,
    val scope: RuleScope,
    val packageName: String? = null,
    val appName: String? = null,
)

data class SilencerSettings(
    val enhancedMarketingRulesEnabled: Boolean = false,
)

interface SilencerRepository {
    fun history(): List<NotificationRecord>
    fun appSummaries(): List<AppSummary>
    fun rules(): List<RuleItem>
    fun settings(): SilencerSettings
    fun setEnhancedMarketingRulesEnabled(enabled: Boolean)
    fun addKeywords(
        keywords: List<String>,
        allow: Boolean,
        scope: RuleScope,
        packageName: String? = null,
        appName: String? = null,
    )
    fun deleteRules(ids: Set<Long>)
    fun deleteHistoryRecord(id: Long)
    fun clearHistory()
}
