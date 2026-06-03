package com.hugo.notificationsilencer.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
