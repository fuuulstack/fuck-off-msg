package com.hugo.notificationsilencer.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test
import com.hugo.notificationsilencer.data.NotificationDecision
import com.hugo.notificationsilencer.data.NotificationRecord

class HistorySnippetTest {
    @Test
    fun blockedSnippetKeepsEightCharactersAroundKeyword() {
        val snippet = notificationSnippet(
            title = "活动提醒",
            body = "今天下单可领取新人礼包，优惠券低至0元，数量有限先到先得",
            keyword = "优惠券",
        )

        assertEquals("可领取新人礼包，优惠券低至0元，数量有", snippet.text)
        assertEquals(8, snippet.highlightStart)
        assertEquals(11, snippet.highlightEnd)
    }

    @Test
    fun normalSnippetFallsBackToSingleLineContent() {
        val snippet = notificationSnippet(
            title = "订单已发货",
            body = "你的包裹正在运输中",
            keyword = null,
        )

        assertEquals("订单已发货 你的包裹正在运输中", snippet.text)
        assertEquals(-1, snippet.highlightStart)
        assertEquals(-1, snippet.highlightEnd)
    }

    @Test
    fun historyFilterCombinesSearchWithBlockedStatus() {
        val records = listOf(
            record(id = 1L, title = "优惠券", decision = NotificationDecision.Blocked),
            record(id = 2L, title = "优惠券", decision = NotificationDecision.Allowed),
            record(id = 3L, title = "验证码", decision = NotificationDecision.Blocked),
        )

        val filtered = filterHistoryRecords(
            records = records,
            query = "优惠",
            decisionFilter = HistoryDecisionFilter.Blocked,
        )

        assertEquals(listOf(1L), filtered.map { it.id })
    }

    @Test
    fun historyFilterAllowedIncludesNonBlockedDecisions() {
        val records = listOf(
            record(id = 1L, decision = NotificationDecision.Blocked),
            record(id = 2L, decision = NotificationDecision.Allowed),
            record(id = 3L, decision = NotificationDecision.WhitelistAllowed),
            record(id = 4L, decision = NotificationDecision.SystemAllowed),
        )

        val filtered = filterHistoryRecords(
            records = records,
            query = "",
            decisionFilter = HistoryDecisionFilter.Allowed,
        )

        assertEquals(listOf(2L, 3L, 4L), filtered.map { it.id })
    }

    @Test
    fun horizontalSwipeBeyondThresholdDeletesInEitherDirection() {
        assertEquals(HistorySwipeAction.Delete, historySwipeAction(totalX = -90f, threshold = 80f))
        assertEquals(HistorySwipeAction.Delete, historySwipeAction(totalX = 90f, threshold = 80f))
        assertEquals(HistorySwipeAction.None, historySwipeAction(totalX = 24f, threshold = 80f))
    }

    private fun record(
        id: Long,
        title: String = "title",
        decision: NotificationDecision,
    ): NotificationRecord {
        return NotificationRecord(
            id = id,
            packageName = "com.example",
            appName = "Example",
            title = title,
            body = "body",
            receivedAt = "0",
            decision = decision,
        )
    }
}
