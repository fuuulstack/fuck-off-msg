package com.hugo.notificationsilencer.service

import com.hugo.notificationsilencer.rules.DefaultKeywords
import com.hugo.notificationsilencer.rules.RuleEngine
import com.hugo.notificationsilencer.rules.RuleResult

class NotificationProcessor(
    private val enhancedEnabled: Boolean = false,
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
        return RuleEngine.evaluate(
            text = snapshot.searchableText,
            packageName = snapshot.packageName,
            systemWhitelist = systemWhitelist,
            userWhitelist = emptySet(),
            userBlacklist = emptySet(),
            conservativeKeywords = DefaultKeywords.Conservative,
            enhancedKeywords = DefaultKeywords.Enhanced,
            enhancedEnabled = enhancedEnabled,
        )
    }
}
