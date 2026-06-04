package com.hugo.notificationsilencer.data

private const val KEY_ENHANCED_MARKETING_RULES_ENABLED = "enhancedMarketingRulesEnabled"

fun SilencerSettings.toJsonString(): String {
    return """{"$KEY_ENHANCED_MARKETING_RULES_ENABLED":$enhancedMarketingRulesEnabled}"""
}

fun String.toSilencerSettings(): SilencerSettings {
    return SilencerSettings(
        enhancedMarketingRulesEnabled = Regex(
            """"$KEY_ENHANCED_MARKETING_RULES_ENABLED"\s*:\s*true""",
        ).containsMatchIn(this),
    )
}
