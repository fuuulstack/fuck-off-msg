package com.hugo.notificationsilencer.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SilencerSettingsCodecTest {
    @Test
    fun settingsRoundTripKeepsEnhancedMarketingRulesFlag() {
        val encoded = SilencerSettings(
            enhancedMarketingRulesEnabled = true,
            appLanguage = AppLanguage.English,
        ).toJsonString()

        val decoded = encoded.toSilencerSettings()

        assertTrue(decoded.enhancedMarketingRulesEnabled)
        assertEquals(AppLanguage.English, decoded.appLanguage)
    }

    @Test
    fun missingSettingsFallBackToDefaults() {
        val decoded = "{}".toSilencerSettings()

        assertFalse(decoded.enhancedMarketingRulesEnabled)
        assertEquals(AppLanguage.System, decoded.appLanguage)
    }
}
