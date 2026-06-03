# 安卓通知静默拦截 App 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 先把一款可运行的安卓 MVP 搭起来，包含通知监听、规则引擎、历史记录、应用聚合、规则管理、涂抹选词，以及 PowerShell 中文编码修复。

**Architecture:** 采用 Kotlin + Jetpack Compose + Room 的本地优先架构。通知监听层只负责采集和取消通知，规则引擎独立做优先级判断，数据层统一保存历史和规则，UI 层只消费状态并发起规则训练。PowerShell 编码修复作为独立环境任务处理，不掺进 App 逻辑。

**Tech Stack:** Kotlin、Jetpack Compose、Room、JUnit、AndroidX Test、NotificationListenerService、Gradle Kotlin DSL。

---

### Task 1: 固定构建环境和 PowerShell 编码

**Files:**
- Modify: `E:\CodexProjects\fuck-off-msg\.worktrees\notification-silencer-mvp\` 下的 PowerShell profile（用户级，不进仓库）
- Modify: `docs/superpowers/plans/2026-06-03-notification-silencer-mvp.md`

- [ ] **Step 1: 先写一个最小验证命令**

```powershell
[Console]::OutputEncoding.WebName
```

- [ ] **Step 2: 让 PowerShell 会话强制使用 UTF-8**

```powershell
$utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $utf8
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
chcp 65001 | Out-Null
```

- [ ] **Step 3: 把同一段设置写入 `$PROFILE`**

```powershell
if (!(Test-Path $PROFILE)) {
  New-Item -ItemType File -Path $PROFILE -Force | Out-Null
}

$snippet = @'
$utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $utf8
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
chcp 65001 | Out-Null
'@

if (-not (Get-Content $PROFILE -Raw -ErrorAction SilentlyContinue).Contains('chcp 65001')) {
  Add-Content -Path $PROFILE -Value "`r`n$snippet`r`n"
}
```

- [ ] **Step 4: 重新打开一个 PowerShell 会话并验证中文输出**

```powershell
Write-Host "中文测试：优惠券 限时秒杀 会员专享"
Get-Content -Raw .\README.md
```

- [ ] **Step 5: 记录结果**

确认控制台里中文不再乱码后再继续下一任务。

### Task 2: 搭建 Android 工程骨架

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradle/wrapper/gradle-wrapper.jar`
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/hugo/notificationsilencer/MainActivity.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/SilencerApp.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/navigation/AppNav.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/theme/Theme.kt`

- [ ] **Step 1: 写一个会失败的构建测试目标**

先把项目目录和 Gradle 配置落地到最小可编译状态，目标是能执行：

```bash
./gradlew :app:assembleDebug
```

- [ ] **Step 2: 先创建最小 Android 入口和 Compose 壳**

`MainActivity` 只承载 `setContent { SilencerApp() }`，`SilencerApp` 只负责挂载导航骨架。

- [ ] **Step 3: 下载并接通 Gradle wrapper**

以 Android Gradle Plugin 和 Kotlin 的稳定版本为准，确保 wrapper 能在当前机器上跑起来。

- [ ] **Step 4: 运行一次构建并修到通过**

```bash
./gradlew :app:assembleDebug
```

- [ ] **Step 5: 提交**

```bash
git add .
git commit -m "chore: bootstrap android app shell"
```

### Task 3: 先写规则引擎测试，再实现匹配逻辑

**Files:**
- Create: `app/src/test/java/com/hugo/notificationsilencer/rules/RuleEngineTest.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/rules/RuleEngine.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/rules/RuleModels.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/rules/DefaultKeywords.kt`

- [ ] **Step 1: 写失败测试**

```kotlin
@Test
fun whitelist_beats_blacklist_and_marketing_rules() {
    val result = RuleEngine.evaluate(
        text = "订单已发货，领取优惠券",
        packageName = "com.example.shop",
        systemWhitelist = setOf("android"),
        userWhitelist = setOf("订单已发货"),
        userBlacklist = setOf("优惠券"),
        conservativeKeywords = setOf("优惠券"),
        enhancedKeywords = emptySet(),
        enhancedEnabled = false,
    )

    assertEquals(Decision.ALLOWED, result.decision)
    assertEquals("订单已发货", result.matchedKeyword)
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
./gradlew testDebugUnitTest --tests com.hugo.notificationsilencer.rules.RuleEngineTest
```

- [ ] **Step 3: 实现最小规则引擎**

先支持：

- 系统白名单
- 用户白名单
- 用户黑名单
- 保守规则
- 增强规则开关
- 命中关键词回传

- [ ] **Step 4: 运行测试确认通过**

```bash
./gradlew testDebugUnitTest --tests com.hugo.notificationsilencer.rules.RuleEngineTest
```

- [ ] **Step 5: 提交**

```bash
git add .
git commit -m "feat: add rule engine"
```

### Task 4: 建立本地数据库和通知历史模型

**Files:**
- Create: `app/src/main/java/com/hugo/notificationsilencer/data/NotificationRecord.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/data/RuleEntity.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/data/AppDatabase.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/data/NotificationDao.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/data/RuleDao.kt`
- Create: `app/src/test/java/com/hugo/notificationsilencer/data/NotificationDaoTest.kt`

- [ ] **Step 1: 写 DAO 行为测试**

验证能保存历史、按应用聚合、按命中状态筛选。

- [ ] **Step 2: 实现 Room 实体和 DAO**

包括历史记录、规则条目、索引和查询方法。

- [ ] **Step 3: 运行数据库测试**

```bash
./gradlew testDebugUnitTest --tests com.hugo.notificationsilencer.data.NotificationDaoTest
```

- [ ] **Step 4: 提交**

```bash
git add .
git commit -m "feat: add local history storage"
```

### Task 5: 接入通知监听与取消流程

**Files:**
- Create: `app/src/main/java/com/hugo/notificationsilencer/service/NotificationListener.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/service/NotificationProcessor.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/service/NotificationSnapshot.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 写通知处理测试**

给一条模拟通知输入，验证系统白名单、用户白名单、用户黑名单和默认关键词的决策结果。

- [ ] **Step 2: 实现通知快照提取**

提取包名、标题、正文、展开文本、时间、key、groupKey、图标。

- [ ] **Step 3: 实现取消和历史写入**

取消命中通知，写入本地历史，记录命中词。

- [ ] **Step 4: 运行测试**

```bash
./gradlew testDebugUnitTest
```

- [ ] **Step 5: 提交**

```bash
git add .
git commit -m "feat: process notifications"
```

### Task 6: 做历史页、应用页、规则页、设置页

**Files:**
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/history/HistoryScreen.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/apps/AppsScreen.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/rules/RulesScreen.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/navigation/AppNav.kt`

- [ ] **Step 1: 写 UI 状态测试**

验证历史页默认平铺、应用页按 App 聚合、规则页能看到默认词组、设置页能看到授权状态。

- [ ] **Step 2: 实现 Compose 页面**

底部四栏导航固定，主屏默认显示历史页。

- [ ] **Step 3: 运行 UI/单元测试**

```bash
./gradlew testDebugUnitTest
```

- [ ] **Step 4: 提交**

```bash
git add .
git commit -m "feat: add main screens"
```

### Task 7: 实现历史页左滑与涂抹选词页

**Files:**
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/mark/MarkScreen.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/mark/SmearSelectionViewModel.kt`
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/mark/SmearSelectionModels.kt`
- Create: `app/src/test/java/com/hugo/notificationsilencer/ui/mark/SmearSelectionTest.kt`

- [ ] **Step 1: 写选词拆分测试**

验证连续涂抹合并为一个词，非连续涂抹拆成多个词，英文数字连续段合并。

- [ ] **Step 2: 实现选词 UI**

左滑历史卡片后点击“标记”进入涂抹选词页，支持文字格子置灰、连续/非连续选区处理、白名单/黑名单作用域弹窗。

- [ ] **Step 3: 运行测试**

```bash
./gradlew testDebugUnitTest --tests com.hugo.notificationsilencer.ui.mark.SmearSelectionTest
```

- [ ] **Step 4: 提交**

```bash
git add .
git commit -m "feat: add smear selection flow"
```

### Task 8: 终验与整理

**Files:**
- Modify: `README.md`
- Modify: `docs/superpowers/specs/2026-06-03-notification-silencer-design.md` if implementation reveals spec mismatch

- [ ] **Step 1: 跑完整测试**

```bash
./gradlew testDebugUnitTest
./gradlew :app:assembleDebug
```

- [ ] **Step 2: 检查历史、规则、通知、选词四条主流程**

确认被拦通知能标注命中词，白名单优先级正确，涂抹页添加后不返回。

- [ ] **Step 3: 只在确有差异时更新规格**

不要为了实现方便随意改动产品边界。

- [ ] **Step 4: 提交收尾**

```bash
git add .
git commit -m "feat: complete notification silencer mvp"
```
