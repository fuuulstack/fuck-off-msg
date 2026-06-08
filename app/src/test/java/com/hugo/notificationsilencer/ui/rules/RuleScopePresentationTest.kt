package com.hugo.notificationsilencer.ui.rules

import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.data.RuleScope
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleScopePresentationTest {
    @Test
    fun currentAppRuleUsesStoredAppIdentityForScopePresentation() {
        val rule = RuleItem(
            id = 1L,
            keyword = "coupon",
            allow = false,
            scope = RuleScope.CurrentApp,
            packageName = "com.example.shop",
            appName = "Example Shop",
        )

        assertEquals(
            RuleScopePresentation.App("com.example.shop", "Example Shop"),
            rule.scopePresentation(),
        )
    }

    @Test
    fun globalRuleUsesGlobalScopePresentation() {
        val rule = RuleItem(
            id = 2L,
            keyword = "shipping",
            allow = true,
            scope = RuleScope.Global,
        )

        assertEquals(RuleScopePresentation.Global, rule.scopePresentation())
    }
}
