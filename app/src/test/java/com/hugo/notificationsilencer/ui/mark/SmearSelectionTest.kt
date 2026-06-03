package com.hugo.notificationsilencer.ui.mark

import org.junit.Assert.assertEquals
import org.junit.Test

class SmearSelectionTest {
    @Test
    fun continuousChineseCellsBecomeOneKeyword() {
        val cells = TextCell.tokenize("领取优惠券")
        val selected = setOf(2, 3, 4)

        val keywords = SmearSelection.groupSelectedKeywords(cells, selected)

        assertEquals(listOf("优惠券"), keywords)
    }

    @Test
    fun nonContinuousSelectionBecomesMultipleKeywords() {
        val cells = TextCell.tokenize("优惠券限时秒杀")
        val selected = setOf(0, 1, 2, 3, 4, 5, 6)

        val keywords = SmearSelection.groupSelectedKeywords(
            cells = cells,
            selectedIndexes = selected,
            breakIndexes = setOf(3),
        )

        assertEquals(listOf("优惠券", "限时秒杀"), keywords)
    }

    @Test
    fun continuousLettersAndNumbersShareOneCell() {
        val cells = TextCell.tokenize("618VIP专享")

        assertEquals(
            listOf("618VIP", "专", "享"),
            cells.map { it.text },
        )
    }
}
