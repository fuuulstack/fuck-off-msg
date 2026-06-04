package com.hugo.notificationsilencer.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SilencerSettingsCodecTest {
    @Test
    fun settingsRoundTripKeepsEnhancedMarketingRulesFlag() {
        val encoded = SilencerSettings(enhancedMarketingRulesEnabled = true).toJsonString()

        val decoded = encoded.toSilencerSettings()

        assertTrue(decoded.enhancedMarketingRulesEnabled)
    }

    @Test
    fun missingSettingsFallBackToDefaults() {
        val decoded = "{}".toSilencerSettings()

        assertFalse(decoded.enhancedMarketingRulesEnabled)
    }
}
