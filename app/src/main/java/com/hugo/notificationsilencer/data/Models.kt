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
)

interface SilencerRepository {
    fun history(): List<NotificationRecord>
    fun appSummaries(): List<AppSummary>
    fun rules(): List<RuleItem>
    fun addKeywords(keywords: List<String>, allow: Boolean, scope: RuleScope)
}
