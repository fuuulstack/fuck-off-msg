package com.hugo.notificationsilencer.data

private const val KEY_ENHANCED_MARKETING_RULES_ENABLED = "enhancedMarketingRulesEnabled"
private const val KEY_APP_LANGUAGE = "appLanguage"

fun SilencerSettings.toJsonString(): String {
    return """{"$KEY_ENHANCED_MARKETING_RULES_ENABLED":$enhancedMarketingRulesEnabled,"$KEY_APP_LANGUAGE":"${appLanguage.name}"}"""
}

fun String.toSilencerSettings(): SilencerSettings {
    return SilencerSettings(
        enhancedMarketingRulesEnabled = Regex(
            """"$KEY_ENHANCED_MARKETING_RULES_ENABLED"\s*:\s*true""",
        ).containsMatchIn(this),
        appLanguage = runCatching {
            val value = Regex(
                """"$KEY_APP_LANGUAGE"\s*:\s*"([^"]+)"""",
            ).find(this)?.groupValues?.get(1).orEmpty()
            AppLanguage.valueOf(value)
        }.getOrDefault(AppLanguage.System),
    )
}
