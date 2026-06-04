package com.hugo.notificationsilencer.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeColorTest {
    @Test
    fun glassCardColorIsOpaqueToAvoidLayeringArtifacts() {
        assertEquals(1f, GlassWhite.alpha, 0f)
    }
}
