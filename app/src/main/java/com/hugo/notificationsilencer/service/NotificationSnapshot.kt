package com.hugo.notificationsilencer.service

data class NotificationSnapshot(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val bigText: String,
    val postTime: Long,
    val groupKey: String?,
    val isGroupSummary: Boolean,
) {
    val searchableText: String
        get() = listOf(title, text, bigText)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")

    val shouldRecord: Boolean
        get() = !isSystemPackage && !isGroupSummary && searchableText.isNotBlank()

    val isSystemPackage: Boolean
        get() = packageName == "android" ||
            packageName.startsWith("com.android.") ||
            packageName.startsWith("com.google.android.") ||
            packageName.startsWith("com.coloros.") ||
            packageName.startsWith("com.oplus.") ||
            packageName.startsWith("com.heytap.")
}
