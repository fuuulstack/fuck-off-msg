package com.hugo.notificationsilencer.ui.rules

import com.hugo.notificationsilencer.data.RuleItem
import com.hugo.notificationsilencer.data.RuleScope

sealed interface RuleScopePresentation {
    data object Global : RuleScopePresentation
    data class App(val packageName: String, val appName: String) : RuleScopePresentation
    data object UnknownApp : RuleScopePresentation
}

fun RuleItem.scopePresentation(): RuleScopePresentation {
    return when (scope) {
        RuleScope.Global -> RuleScopePresentation.Global
        RuleScope.CurrentApp -> {
            val packageName = packageName?.takeIf { it.isNotBlank() }
            val appName = appName?.takeIf { it.isNotBlank() }
            if (packageName != null && appName != null) {
                RuleScopePresentation.App(packageName = packageName, appName = appName)
            } else {
                RuleScopePresentation.UnknownApp
            }
        }
    }
}
