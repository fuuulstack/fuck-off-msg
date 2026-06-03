package com.hugo.pushtester

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val CHANNEL_ID = "push_tester_samples"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            PushTesterApp()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "测试推送",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "用于测试通知静默拦截 App"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}

@Composable
private fun PushTesterApp() {
    val context = LocalContext.current
    var title by remember { mutableStateOf("限时秒杀") }
    var body by remember { mutableStateOf("新人礼包免费领取，优惠券低至 0 元") }
    var counter by remember { mutableStateOf(1) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )

    LaunchedEffect(Unit) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "推送测试器", style = MaterialTheme.typography.headlineMedium)
            Text(text = "安装主 App 并开启通知使用权后，用这些按钮发送通知，检查是否被准确拦截。")

            SampleButton(
                title = "营销推送",
                body = "限时秒杀，新人礼包免费领取，优惠券低至 0 元",
                expected = "预期：被拦截，历史显示命中“优惠券”或“限时秒杀”",
                onSend = { sampleTitle, sampleBody ->
                    sendNotification(context, counter++, sampleTitle, sampleBody)
                },
            )
            SampleButton(
                title = "增强营销推送",
                body = "今日活动为你精选，直播中，任务奖励等你解锁",
                expected = "预期：增强规则开启后被拦截；未开启时可放行",
                onSend = { sampleTitle, sampleBody ->
                    sendNotification(context, counter++, sampleTitle, sampleBody)
                },
            )
            SampleButton(
                title = "物流正常推送",
                body = "订单已发货，你的包裹正在运输中",
                expected = "预期：默认放行；加入白名单后应显示白名单放行",
                onSend = { sampleTitle, sampleBody ->
                    sendNotification(context, counter++, sampleTitle, sampleBody)
                },
            )
            SampleButton(
                title = "安全提醒",
                body = "账号在新设备登录，请确认是否本人操作",
                expected = "预期：放行",
                onSend = { sampleTitle, sampleBody ->
                    sendNotification(context, counter++, sampleTitle, sampleBody)
                },
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "自定义推送", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("正文") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                    Button(onClick = { sendNotification(context, counter++, title, body) }) {
                        Text("发送自定义推送")
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleButton(
    title: String,
    body: String,
    expected: String,
    onSend: (String, String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body)
            Text(text = expected, style = MaterialTheme.typography.bodySmall)
            Row {
                Button(onClick = { onSend(title, body) }) {
                    Text("发送")
                }
            }
        }
    }
}

private fun sendNotification(context: Context, id: Int, title: String, body: String) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(id, notification)
}
