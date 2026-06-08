package com.hugo.notificationsilencer.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RuleEngineTest {
    @Test
    fun systemWhitelistIsAlwaysAllowedFirst() {
        val result = RuleEngine.evaluate(
            text = "限时秒杀 优惠券",
            packageName = "android",
            systemWhitelist = setOf("android"),
            userWhitelist = emptySet(),
            userBlacklist = setOf("优惠券"),
            conservativeKeywords = setOf("优惠券"),
            enhancedKeywords = setOf("活动"),
            enhancedEnabled = true,
        )

        assertEquals(Decision.SYSTEM_ALLOWED, result.decision)
        assertEquals("android", result.matchedKeyword)
        assertEquals(RuleSource.SYSTEM, result.source)
    }

    @Test
    fun whitelistBeatsBlacklistAndMarketingRules() {
        val result = RuleEngine.evaluate(
            text = "订单已发货，领取优惠券",
            packageName = "com.example.shop",
            systemWhitelist = setOf("android"),
            userWhitelist = setOf("订单已发货"),
            userBlacklist = setOf("优惠券"),
            conservativeKeywords = setOf("优惠券"),
            enhancedKeywords = emptySet(),
            enhancedEnabled = false,
        )

        assertEquals(Decision.WHITELIST_ALLOWED, result.decision)
        assertEquals("订单已发货", result.matchedKeyword)
        assertEquals(RuleSource.USER_WHITELIST, result.source)
    }

    @Test
    fun userBlacklistBlocksBeforeDefaultMarketingRules() {
        val result = RuleEngine.evaluate(
            text = "今晚直播中，会员福利别错过",
            packageName = "com.example.video",
            systemWhitelist = emptySet(),
            userWhitelist = emptySet(),
            userBlacklist = setOf("直播中"),
            conservativeKeywords = setOf("会员福利"),
            enhancedKeywords = setOf("福利"),
            enhancedEnabled = true,
        )

        assertEquals(Decision.BLOCKED, result.decision)
        assertEquals("直播中", result.matchedKeyword)
        assertEquals(RuleSource.USER_BLACKLIST, result.source)
    }

    @Test
    fun conservativeKeywordBlocksByDefault() {
        val result = RuleEngine.evaluate(
            text = "新人礼包 免费领取",
            packageName = "com.example.shop",
            systemWhitelist = emptySet(),
            userWhitelist = emptySet(),
            userBlacklist = emptySet(),
            conservativeKeywords = setOf("免费领取"),
            enhancedKeywords = emptySet(),
            enhancedEnabled = false,
        )

        assertEquals(Decision.BLOCKED, result.decision)
        assertEquals("免费领取", result.matchedKeyword)
        assertEquals(RuleSource.DEFAULT_CONSERVATIVE, result.source)
    }

    @Test
    fun enhancedKeywordsOnlyApplyWhenEnabled() {
        val disabled = RuleEngine.evaluate(
            text = "今日活动为你精选",
            packageName = "com.example.shop",
            systemWhitelist = emptySet(),
            userWhitelist = emptySet(),
            userBlacklist = emptySet(),
            conservativeKeywords = emptySet(),
            enhancedKeywords = setOf("活动"),
            enhancedEnabled = false,
        )

        val enabled = RuleEngine.evaluate(
            text = "今日活动为你精选",
            packageName = "com.example.shop",
            systemWhitelist = emptySet(),
            userWhitelist = emptySet(),
            userBlacklist = emptySet(),
            conservativeKeywords = emptySet(),
            enhancedKeywords = setOf("活动"),
            enhancedEnabled = true,
        )

        assertEquals(Decision.ALLOWED, disabled.decision)
        assertNull(disabled.matchedKeyword)
        assertEquals(Decision.BLOCKED, enabled.decision)
        assertEquals("活动", enabled.matchedKeyword)
        assertEquals(RuleSource.DEFAULT_ENHANCED, enabled.source)
    }
}
