package com.hugo.pushtester

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdbNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureNotificationChannel(context)
        val title = intent.getStringExtra("title") ?: "限时秒杀"
        val body = intent.getStringExtra("body") ?: "新人礼包免费领取，优惠券低至 0 元"
        val id = intent.getIntExtra("id", System.currentTimeMillis().toInt())
        val sent = sendNotification(context, id, title, body)
        resultCode = if (sent) 1 else 0
        resultData = if (sent) "sent" else "notification permission missing"
    }
}
