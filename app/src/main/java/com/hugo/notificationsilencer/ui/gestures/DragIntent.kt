package com.hugo.notificationsilencer.ui.gestures

enum class DragIntent {
    Undecided,
    VerticalScroll,
    HorizontalAction,
}

fun dragIntent(totalX: Float, totalY: Float, touchSlop: Float): DragIntent {
    val absX = kotlin.math.abs(totalX)
    val absY = kotlin.math.abs(totalY)
    if (absX < touchSlop && absY < touchSlop) return DragIntent.Undecided
    return if (absY > absX) DragIntent.VerticalScroll else DragIntent.HorizontalAction
}
