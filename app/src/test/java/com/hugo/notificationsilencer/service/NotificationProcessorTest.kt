package com.hugo.notificationsilencer.service

import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.data.RuleScope
import com.hugo.notificationsilencer.data.APP_WIDE_BLOCK_KEYWORD
import com.hugo.notificationsilencer.data.AppLanguage
import com.hugo.notificationsilencer.rules.Decision
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationProcessorTest {
    @Test
    fun evaluatesGlobalUserBlacklistRules() {
        val processor = NotificationProcessor(
            userRules = listOf(
                RuleItem(
                    id = 1L,
                    keyword = "coupon",
                    allow = false,
                    scope = RuleScope.Global,
                ),
            ),
        )

        val result = processor.evaluate(snapshot(packageName = "com.example.shop", text = "new coupon"))

        assertEquals(Decision.BLOCKED, result.decision)
    }

    @Test
    fun currentAppRulesOnlyApplyToTheirPackage() {
        val processor = NotificationProcessor(
            userRules = listOf(
                RuleItem(
                    id = 1L,
                    keyword = "coupon",
                    allow = false,
                    scope = RuleScope.CurrentApp,
                    packageName = "com.example.shop",
                    appName = "Example Shop",
                ),
            ),
        )

        val otherAppResult = processor.evaluate(snapshot(packageName = "com.example.chat", text = "new coupon"))
        val scopedAppResult = processor.evaluate(snapshot(packageName = "com.example.shop", text = "new coupon"))

        assertEquals(Decision.ALLOWED, otherAppResult.decision)
        assertEquals(Decision.BLOCKED, scopedAppResult.decision)
    }

    @Test
    fun currentAppWideBlockRuleBlocksAllNotificationsFromThatPackage() {
        val processor = NotificationProcessor(
            userRules = listOf(
                RuleItem(
                    id = 1L,
                    keyword = APP_WIDE_BLOCK_KEYWORD,
                    allow = false,
                    scope = RuleScope.CurrentApp,
                    packageName = "com.example.noisy",
                    appName = "Noisy App",
                ),
            ),
        )

        val otherAppResult = processor.evaluate(snapshot(packageName = "com.example.chat", text = "ordinary message"))
        val blockedAppResult = processor.evaluate(snapshot(packageName = "com.example.noisy", text = "ordinary message"))

        assertEquals(Decision.ALLOWED, otherAppResult.decision)
        assertEquals(Decision.BLOCKED, blockedAppResult.decision)
    }

    @Test
    fun englishDefaultConservativeRulesBlockMarketingNotifications() {
        val processor = NotificationProcessor(appLanguage = AppLanguage.English)

        val result = processor.evaluate(
            snapshot(packageName = "com.example.shop", text = "Flash sale ends tonight. Use your coupon now."),
        )

        assertEquals(Decision.BLOCKED, result.decision)
    }

    private fun snapshot(packageName: String, text: String): NotificationSnapshot {
        return NotificationSnapshot(
            key = "key",
            packageName = packageName,
            title = "",
            text = text,
            bigText = "",
            postTime = 0L,
            groupKey = null,
            isGroupSummary = false,
        )
    }
}
