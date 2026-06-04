package com.hugo.notificationsilencer.data

class InMemorySilencerRepository private constructor(
    private val historyRecords: MutableList<NotificationRecord>,
    private val ruleItems: MutableList<RuleItem>,
    private var settings: SilencerSettings = SilencerSettings(),
) : SilencerRepository {
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
        keywords.distinct().forEachIndexed { index, keyword ->
            ruleItems += RuleItem(
                id = nextId + index,
                keyword = keyword,
                allow = allow,
                scope = scope,
                packageName = scopedPackageName,
                appName = scopedAppName,
            )
        }
    }

    override fun deleteRules(ids: Set<Long>) {
        ruleItems.removeAll { ids.contains(it.id) }
    }

    override fun deleteHistoryRecord(id: Long) {
        historyRecords.removeAll { it.id == id }
    }

    override fun clearHistory() {
        historyRecords.clear()
    }

    companion object {
        fun sample(): InMemorySilencerRepository {
            return InMemorySilencerRepository(
                historyRecords = mutableListOf(
                    NotificationRecord(
                        id = 1,
                        notificationKey = "sample-shopping",
                        packageName = "com.shop.demo",
                        appName = "购物示例",
                        title = "限时秒杀",
                        body = "新人礼包免费领取，优惠券低至 0 元",
                        receivedAt = "22:10",
                        decision = NotificationDecision.Blocked,
                        matchedKeyword = "优惠券",
                    ),
                    NotificationRecord(
                        id = 2,
                        notificationKey = "sample-delivery",
                        packageName = "com.delivery.demo",
                        appName = "物流示例",
                        title = "订单已发货",
                        body = "你的包裹正在运输中",
                        receivedAt = "21:48",
                        decision = NotificationDecision.WhitelistAllowed,
                        matchedKeyword = "订单已发货",
                    ),
                    NotificationRecord(
                        id = 3,
                        notificationKey = "sample-system",
                        packageName = "android",
                        appName = "Android 系统",
                        title = "系统更新",
                        body = "设备安全更新已准备好",
                        receivedAt = "20:05",
                        decision = NotificationDecision.SystemAllowed,
                    ),
                ),
                ruleItems = mutableListOf(
                    RuleItem(1, "订单已发货", allow = true, scope = RuleScope.Global),
                    RuleItem(2, "优惠券", allow = false, scope = RuleScope.Global),
                ),
            )
        }
    }
}
