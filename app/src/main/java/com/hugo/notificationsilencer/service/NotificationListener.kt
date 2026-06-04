package com.hugo.notificationsilencer.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.hugo.notificationsilencer.data.SilencerStore
import com.hugo.notificationsilencer.rules.Decision

class NotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationListenerHealth.markConnected()
        Log.i(TAG, "Notification listener connected")
        activeNotifications.orEmpty().forEach { handleNotification(it, source = "active") }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationListenerHealth.markDisconnected()
        Log.i(TAG, "Notification listener disconnected")
    }

    override fun onDestroy() {
        NotificationListenerHealth.markDisconnected()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationListenerHealth.markNotificationReceived()
        handleNotification(sbn, source = "posted")
    }

    private fun handleNotification(sbn: StatusBarNotification, source: String) {
        val snapshot = sbn.toSnapshot()
        if (!snapshot.shouldRecord) {
            Log.i(TAG, "Ignoring notification $source package=${snapshot.packageName} groupSummary=${snapshot.isGroupSummary}")
            return
        }
        val settings = SilencerStore.loadSettings(this)
        val processor = NotificationProcessor(
            enhancedEnabled = settings.enhancedMarketingRulesEnabled,
            userRules = SilencerStore.loadRules(this),
        )
        val result = processor.evaluate(snapshot)
        Log.i(
            TAG,
            "Notification $source package=${snapshot.packageName} decision=${result.decision} keyword=${result.matchedKeyword}",
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
        isGroupSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
    )
}
