package com.hugo.notificationsilencer.service

import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.data.RuleScope
import com.hugo.notificationsilencer.data.APP_WIDE_BLOCK_LABEL
import com.hugo.notificationsilencer.data.AppLanguage
import com.hugo.notificationsilencer.data.isAppWideBlockRule
import com.hugo.notificationsilencer.rules.DefaultKeywords
import com.hugo.notificationsilencer.rules.RuleEngine
import com.hugo.notificationsilencer.rules.RuleResult

class NotificationProcessor(
    private val enhancedEnabled: Boolean = false,
    private val userRules: List<RuleItem> = emptyList(),
    private val appLanguage: AppLanguage = AppLanguage.System,
) {
    private val systemWhitelist = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.dialer",
        "com.android.dialer",
        "com.android.phone",
        "com.google.android.apps.messaging",
        "com.android.mms",
    )

    fun evaluate(snapshot: NotificationSnapshot): RuleResult {
        val relevantRules = userRules.filter { rule ->
            rule.scope == RuleScope.Global ||
                (rule.scope == RuleScope.CurrentApp && rule.packageName == snapshot.packageName)
        }
        val appWideBlacklistMatchedKeyword = relevantRules
            .firstOrNull { it.isAppWideBlockRule() }
            ?.let { APP_WIDE_BLOCK_LABEL }
        return RuleEngine.evaluate(
            text = snapshot.searchableText,
            packageName = snapshot.packageName,
            systemWhitelist = systemWhitelist,
            userWhitelist = relevantRules.filter { it.allow }.map { it.keyword }.toSet(),
            userBlacklist = relevantRules.filter { !it.allow && !it.isAppWideBlockRule() }.map { it.keyword }.toSet(),
            conservativeKeywords = DefaultKeywords.conservativeFor(appLanguage),
            enhancedKeywords = DefaultKeywords.enhancedFor(appLanguage),
            enhancedEnabled = enhancedEnabled,
            appWideBlacklistMatchedKeyword = appWideBlacklistMatchedKeyword,
        )
    }
}
