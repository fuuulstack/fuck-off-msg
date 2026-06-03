package com.hugo.notificationsilencer.rules

object RuleEngine {
    fun evaluate(
        text: String,
        packageName: String,
        systemWhitelist: Set<String>,
        userWhitelist: Set<String>,
        userBlacklist: Set<String>,
        conservativeKeywords: Set<String>,
        enhancedKeywords: Set<String>,
        enhancedEnabled: Boolean,
    ): RuleResult {
        if (systemWhitelist.contains(packageName)) {
            return RuleResult(
                decision = Decision.SYSTEM_ALLOWED,
                matchedKeyword = packageName,
                source = RuleSource.SYSTEM,
            )
        }

        userWhitelist.firstMatchIn(text)?.let { keyword ->
            return RuleResult(
                decision = Decision.WHITELIST_ALLOWED,
                matchedKeyword = keyword,
                source = RuleSource.USER_WHITELIST,
            )
        }

        userBlacklist.firstMatchIn(text)?.let { keyword ->
            return RuleResult(
                decision = Decision.BLOCKED,
                matchedKeyword = keyword,
                source = RuleSource.USER_BLACKLIST,
            )
        }

        conservativeKeywords.firstMatchIn(text)?.let { keyword ->
            return RuleResult(
                decision = Decision.BLOCKED,
                matchedKeyword = keyword,
                source = RuleSource.DEFAULT_CONSERVATIVE,
            )
        }

        if (enhancedEnabled) {
            enhancedKeywords.firstMatchIn(text)?.let { keyword ->
                return RuleResult(
                    decision = Decision.BLOCKED,
                    matchedKeyword = keyword,
                    source = RuleSource.DEFAULT_ENHANCED,
                )
            }
        }

        return RuleResult(decision = Decision.ALLOWED)
    }
}

private fun Set<String>.firstMatchIn(text: String): String? {
    return firstOrNull { keyword ->
        keyword.isNotBlank() && text.contains(keyword, ignoreCase = true)
    }
}
