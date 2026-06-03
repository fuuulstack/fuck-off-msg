package com.hugo.notificationsilencer.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.hugo.notificationsilencer.data.SilencerStore
import com.hugo.notificationsilencer.rules.Decision

class NotificationListener : NotificationListenerService() {
    private val processor = NotificationProcessor()

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val snapshot = sbn.toSnapshot()
        val result = processor.evaluate(snapshot)
        Log.i(
            TAG,
            "Notification posted package=${snapshot.packageName} decision=${result.decision} keyword=${result.matchedKeyword}",
        )
        SilencerStore.appendNotification(this, snapshot, result)

        if (result.decision == Decision.BLOCKED) {
            Log.i(TAG, "Canceling notification key=${snapshot.key}")
            cancelNotification(snapshot.key)
        }
    }

    companion object {
        private const val TAG = "SilencerListener"
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
