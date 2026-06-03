package com.hugo.notificationsilencer.service

data class NotificationSnapshot(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val bigText: String,
    val postTime: Long,
    val groupKey: String?,
) {
    val searchableText: String
        get() = listOf(title, text, bigText)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
}
