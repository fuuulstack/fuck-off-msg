package com.hugo.notificationsilencer.ui.selection

fun selectAllIds(ids: Collection<Long>): Set<Long> {
    return ids.toSet()
}

fun invertSelectedIds(ids: Collection<Long>, selectedIds: Set<Long>): Set<Long> {
    return ids.filterNot { selectedIds.contains(it) }.toSet()
}
