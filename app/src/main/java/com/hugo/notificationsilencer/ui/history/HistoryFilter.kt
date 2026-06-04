package com.hugo.notificationsilencer.ui.history

import com.hugo.notificationsilencer.data.NotificationDecision
import com.hugo.notificationsilencer.data.NotificationRecord

enum class HistoryDecisionFilter {
    All,
    Blocked,
    Allowed,
}

enum class HistorySwipeAction {
    None,
    Delete,
}

fun historySwipeAction(totalX: Float, threshold: Float): HistorySwipeAction {
    return if (kotlin.math.abs(totalX) >= threshold) {
        HistorySwipeAction.Delete
    } else {
        HistorySwipeAction.None
    }
}

fun filterHistoryRecords(
    records: List<NotificationRecord>,
    query: String,
    decisionFilter: HistoryDecisionFilter,
): List<NotificationRecord> {
    return records
        .filter { it.matchesQuery(query) }
        .filter { record ->
            when (decisionFilter) {
                HistoryDecisionFilter.All -> true
                HistoryDecisionFilter.Blocked -> record.decision == NotificationDecision.Blocked
                HistoryDecisionFilter.Allowed -> record.decision != NotificationDecision.Blocked
            }
        }
}

fun NotificationRecord.matchesQuery(query: String): Boolean {
    val normalized = query.trim()
    if (normalized.isEmpty()) return true
    return listOf(appName, packageName, title, body, matchedKeyword.orEmpty())
        .any { it.contains(normalized, ignoreCase = true) }
}
