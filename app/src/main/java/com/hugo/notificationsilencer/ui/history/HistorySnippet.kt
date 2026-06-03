package com.hugo.notificationsilencer.ui.history

data class NotificationSnippet(
    val text: String,
    val highlightStart: Int,
    val highlightEnd: Int,
)

fun notificationSnippet(
    title: String,
    body: String,
    keyword: String?,
    contextChars: Int = 8,
): NotificationSnippet {
    val content = listOf(title, body)
        .map { it.trim() }
        .filter { it.isNotBlank() && it != "(空通知)" && it != "(无标题)" }
        .distinct()
        .joinToString(" ")
        .ifBlank { "(空通知)" }

    if (keyword.isNullOrBlank()) {
        return NotificationSnippet(content, -1, -1)
    }

    val keywordStart = content.indexOf(keyword)
    if (keywordStart < 0) {
        return NotificationSnippet(content, -1, -1)
    }

    val keywordEnd = keywordStart + keyword.length
    val snippetStart = (keywordStart - contextChars).coerceAtLeast(0)
    val snippetEnd = (keywordEnd + contextChars).coerceAtMost(content.length)
    val snippetText = content.substring(snippetStart, snippetEnd)

    return NotificationSnippet(
        text = snippetText,
        highlightStart = keywordStart - snippetStart,
        highlightEnd = keywordEnd - snippetStart,
    )
}
