package com.hugo.notificationsilencer.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.hugo.notificationsilencer.rules.Decision

class NotificationListener : NotificationListenerService() {
    private val processor = NotificationProcessor()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val snapshot = sbn.toSnapshot()
        val result = processor.evaluate(snapshot)

        if (result.decision == Decision.BLOCKED) {
            cancelNotification(snapshot.key)
        }
    }
}

private fun StatusBarNotification.toSnapshot(): NotificationSnapshot {
    val extras = notification.extras
    return NotificationSnapshot(
        key = key,
        packageName = packageName,
        title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
        text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
        bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty(),
        postTime = postTime,
        groupKey = groupKey,
    )
}
