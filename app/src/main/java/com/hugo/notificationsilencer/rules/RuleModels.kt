package com.hugo.notificationsilencer.rules

enum class Decision {
    ALLOWED,
    BLOCKED,
    SYSTEM_ALLOWED,
    WHITELIST_ALLOWED,
}

enum class RuleSource {
    NONE,
    SYSTEM,
    USER_WHITELIST,
    USER_BLACKLIST,
    DEFAULT_CONSERVATIVE,
    DEFAULT_ENHANCED,
}

data class RuleResult(
    val decision: Decision,
    val matchedKeyword: String? = null,
    val source: RuleSource = RuleSource.NONE,
)
