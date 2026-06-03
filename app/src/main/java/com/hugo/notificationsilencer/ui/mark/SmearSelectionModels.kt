package com.hugo.notificationsilencer.ui.mark

data class TextCell(
    val index: Int,
    val text: String,
    val added: Boolean = false,
) {
    companion object {
        fun tokenize(text: String): List<TextCell> {
            val cells = mutableListOf<TextCell>()
            val buffer = StringBuilder()

            fun flushBuffer() {
                if (buffer.isNotEmpty()) {
                    cells += TextCell(cells.size, buffer.toString())
                    buffer.clear()
                }
            }

            text.forEach { char ->
                if (char.isAsciiLetterOrDigit() || char == '.') {
                    buffer.append(char)
                } else {
                    flushBuffer()
                    if (!char.isWhitespace()) {
                        cells += TextCell(cells.size, char.toString())
                    }
                }
            }
            flushBuffer()

            return cells
        }
    }
}

object SmearSelection {
    fun groupSelectedKeywords(
        cells: List<TextCell>,
        selectedIndexes: Set<Int>,
        breakIndexes: Set<Int> = emptySet(),
    ): List<String> {
        if (selectedIndexes.isEmpty()) return emptyList()

        val sortedIndexes = selectedIndexes.sorted()
        val ranges = mutableListOf<MutableList<Int>>()
        var current = mutableListOf<Int>()

        sortedIndexes.forEach { index ->
            val previous = current.lastOrNull()
            val shouldStartNewRange = previous == null ||
                index != previous + 1 ||
                breakIndexes.contains(index)

            if (shouldStartNewRange) {
                if (current.isNotEmpty()) ranges += current
                current = mutableListOf(index)
            } else {
                current += index
            }
        }
        if (current.isNotEmpty()) ranges += current

        return ranges.mapNotNull { range ->
            range.joinToString(separator = "") { index -> cells[index].text }
                .takeIf { it.isNotBlank() }
        }
    }
}

private fun Char.isAsciiLetterOrDigit(): Boolean {
    return this in '0'..'9' || this in 'a'..'z' || this in 'A'..'Z'
}
