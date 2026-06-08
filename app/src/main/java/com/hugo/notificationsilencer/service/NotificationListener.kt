package com.hugo.notificationsilencer.service

import android.app.Notification
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.NotificationListenerService.RankingMap
import android.service.notification.StatusBarNotification
import android.util.Log
import com.hugo.notificationsilencer.data.SilencerStore
import com.hugo.notificationsilencer.rules.Decision

class NotificationListener : NotificationListenerService() {
    private val handler = Handler(Looper.getMainLooper())
    private var activeScanScheduled = false
    private val periodicActiveScan = object : Runnable {
        override fun run() {
            scanActiveNotifications(source = "periodic", recordHistory = false)
            handler.postDelayed(this, PERIODIC_ACTIVE_SCAN_INTERVAL_MS)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationListenerHealth.markConnected()
        Log.i(TAG, "Notification listener connected")
        scanActiveNotifications(source = "active")
        startPeriodicActiveScan()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationListenerHealth.markDisconnected()
        Log.i(TAG, "Notification listener disconnected")
        stopPeriodicActiveScan()
    }

    override fun onDestroy() {
        NotificationListenerHealth.markDisconnected()
        stopPeriodicActiveScan()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationListenerHealth.markNotificationReceived()
        handleNotification(sbn, source = "posted")
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap) {
        super.onNotificationRankingUpdate(rankingMap)
        scheduleActiveScan(source = "ranking")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        scheduleActiveScan(source = "removed")
    }

    private fun handleNotification(
        sbn: StatusBarNotification,
        source: String,
        recordHistory: Boolean = true,
    ) {
        val snapshot = sbn.toSnapshot()
        if (!snapshot.shouldRecord) {
            Log.i(TAG, "Ignoring notification $source package=${snapshot.packageName} groupSummary=${snapshot.isGroupSummary}")
            return
        }
        val settings = SilencerStore.loadSettings(this)
        val processor = NotificationProcessor(
            enhancedEnabled = settings.enhancedMarketingRulesEnabled,
            userRules = SilencerStore.loadRules(this),
            appLanguage = settings.appLanguage,
        )
        val result = processor.evaluate(snapshot)
        Log.i(
            TAG,
            "Notification $source package=${snapshot.packageName} decision=${result.decision} keyword=${result.matchedKeyword}",
        )
        if (recordHistory) {
            SilencerStore.appendNotification(this, snapshot, result)
        }

        if (result.decision == Decision.BLOCKED) {
            cancelBlockedNotification(snapshot.key, source)
        }
    }

    private fun scheduleActiveScan(source: String) {
        if (activeScanScheduled) return
        activeScanScheduled = true
        handler.postDelayed(
            {
                activeScanScheduled = false
                scanActiveNotifications(source = source)
            },
            ACTIVE_SCAN_DELAY_MS,
        )
    }

    private fun scanActiveNotifications(
        source: String,
        recordHistory: Boolean = true,
    ) {
        val notifications = runCatching { activeNotifications.orEmpty() }
            .onFailure { Log.w(TAG, "Unable to read active notifications source=$source", it) }
            .getOrElse { emptyArray() }
        Log.i(TAG, "Scanning active notifications source=$source count=${notifications.size}")
        notifications.forEach { handleNotification(it, source = source, recordHistory = recordHistory) }
    }

    private fun startPeriodicActiveScan() {
        handler.removeCallbacks(periodicActiveScan)
        handler.postDelayed(periodicActiveScan, PERIODIC_ACTIVE_SCAN_INTERVAL_MS)
    }

    private fun stopPeriodicActiveScan() {
        handler.removeCallbacks(periodicActiveScan)
        activeScanScheduled = false
    }

    private fun cancelBlockedNotification(key: String, source: String) {
        Log.i(TAG, "Canceling notification source=$source key=$key")
        cancelNotification(key)
        RETRY_CANCEL_DELAYS_MS.forEach { delay ->
            handler.postDelayed(
                {
                    Log.i(TAG, "Retry canceling notification delay=${delay}ms key=$key")
                    cancelNotification(key)
                },
                delay,
            )
        }
    }

    companion object {
        private const val TAG = "SilencerListener"
        private const val ACTIVE_SCAN_DELAY_MS = 120L
        private const val PERIODIC_ACTIVE_SCAN_INTERVAL_MS = 30_000L
        private val RETRY_CANCEL_DELAYS_MS = longArrayOf(120L, 500L, 1500L)
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
