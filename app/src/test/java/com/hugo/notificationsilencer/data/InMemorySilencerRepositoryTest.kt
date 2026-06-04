package com.hugo.notificationsilencer.data

import org.junit.Assert.assertEquals
import org.junit.Test

class InMemorySilencerRepositoryTest {
    @Test
    fun deleteRulesRemovesMatchingRulesOnly() {
        val repository = InMemorySilencerRepository.sample()

        repository.deleteRules(setOf(1L))

        assertEquals(listOf(2L), repository.rules().map { it.id })
    }
}
