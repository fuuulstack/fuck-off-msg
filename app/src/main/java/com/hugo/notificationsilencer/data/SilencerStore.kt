package com.hugo.notificationsilencer.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.hugo.notificationsilencer.rules.RuleResult
import com.hugo.notificationsilencer.service.NotificationSnapshot
import org.json.JSONArray
import org.json.JSONObject

object SilencerStore {
    private const val PREFS = "notification_silencer_store"
    private const val KEY_HISTORY = "history"
    private const val KEY_RULES = "rules"

    fun loadHistory(context: Context): List<NotificationRecord> {
        val array = JSONArray(context.prefs().getString(KEY_HISTORY, "[]"))
        return (0 until array.length()).map { index ->
            array.getJSONObject(index).toNotificationRecord()
        }
    }

    fun loadRules(context: Context): List<RuleItem> {
        val array = JSONArray(context.prefs().getString(KEY_RULES, "[]"))
        return (0 until array.length()).map { index ->
            array.getJSONObject(index).toRuleItem()
        }
    }

    fun saveHistory(context: Context, records: List<NotificationRecord>) {
        val array = JSONArray()
        records.forEach { array.put(it.toJson()) }
        context.prefs().edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun saveRules(context: Context, rules: List<RuleItem>) {
        val array = JSONArray()
        rules.forEach { array.put(it.toJson()) }
        context.prefs().edit().putString(KEY_RULES, array.toString()).apply()
    }

    fun appendNotification(
        context: Context,
        snapshot: NotificationSnapshot,
        result: RuleResult,
    ) {
        val records = loadHistory(context).toMutableList()
        val record = NotificationRecord(
            id = System.currentTimeMillis(),
            packageName = snapshot.packageName,
            appName = context.resolveAppName(snapshot.packageName),
            title = snapshot.title.ifBlank { "(无标题)" },
            body = snapshot.searchableText.ifBlank { "(空通知)" },
            receivedAt = snapshot.postTime.toString(),
            decision = result.toNotificationDecision(),
            matchedKeyword = result.matchedKeyword,
        )
        records.removeAll { it.id == record.id }
        records += record
        saveHistory(context, records.takeLast(500))
    }

    private fun Context.prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

private fun Context.resolveAppName(packageName: String): String {
    return runCatching {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, 0)
        }
        info.loadLabel(packageManager).toString()
    }.getOrElse { packageName }
}

private fun RuleResult.toNotificationDecision(): NotificationDecision {
    return when (decision) {
        com.hugo.notificationsilencer.rules.Decision.ALLOWED -> NotificationDecision.Allowed
        com.hugo.notificationsilencer.rules.Decision.BLOCKED -> NotificationDecision.Blocked
        com.hugo.notificationsilencer.rules.Decision.SYSTEM_ALLOWED -> NotificationDecision.SystemAllowed
        com.hugo.notificationsilencer.rules.Decision.WHITELIST_ALLOWED -> NotificationDecision.WhitelistAllowed
    }
}

private fun NotificationRecord.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("packageName", packageName)
        .put("appName", appName)
        .put("title", title)
        .put("body", body)
        .put("receivedAt", receivedAt)
        .put("decision", decision.name)
        .put("matchedKeyword", matchedKeyword)
}

private fun JSONObject.toNotificationRecord(): NotificationRecord {
    return NotificationRecord(
        id = getLong("id"),
        packageName = getString("packageName"),
        appName = getString("appName"),
        title = getString("title"),
        body = getString("body"),
        receivedAt = getString("receivedAt"),
        decision = NotificationDecision.valueOf(getString("decision")),
        matchedKeyword = optString("matchedKeyword").takeIf { it.isNotBlank() && it != "null" },
    )
}

private fun RuleItem.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("keyword", keyword)
        .put("allow", allow)
        .put("scope", scope.name)
}

private fun JSONObject.toRuleItem(): RuleItem {
    return RuleItem(
        id = getLong("id"),
        keyword = getString("keyword"),
        allow = getBoolean("allow"),
        scope = RuleScope.valueOf(getString("scope")),
    )
}
