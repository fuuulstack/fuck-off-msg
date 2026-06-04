package com.hugo.notificationsilencer.ui.selection

import org.junit.Assert.assertEquals
import org.junit.Test

class SelectionModelsTest {
    @Test
    fun selectAllReturnsEveryVisibleId() {
        assertEquals(setOf(1L, 2L, 3L), selectAllIds(listOf(1L, 2L, 3L)))
    }

    @Test
    fun invertSelectedIdsSelectsOnlyUnselectedVisibleIds() {
        assertEquals(setOf(1L, 3L), invertSelectedIds(listOf(1L, 2L, 3L), selectedIds = setOf(2L)))
    }
}
