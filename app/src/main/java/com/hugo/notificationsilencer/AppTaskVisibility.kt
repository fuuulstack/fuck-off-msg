package com.hugo.notificationsilencer

object AppTaskVisibility {
    var skipNextTaskRemoval: Boolean = false

    fun consumeSkipNextTaskRemoval(): Boolean {
        val shouldSkip = skipNextTaskRemoval
        skipNextTaskRemoval = false
        return shouldSkip
    }
}
