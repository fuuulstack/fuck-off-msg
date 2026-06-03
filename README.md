# fuck-off-msg

安卓通知静默拦截 App。

当前分支实现了 MVP 骨架：

- 通知监听服务声明。
- 系统白名单、用户白名单、用户黑名单、默认营销词、增强营销词的规则引擎。
- 默认营销关键词词库。
- 历史页、应用页、规则页、设置页。
- 涂抹选词页：连续选择合并为一个词，非连续选择拆成多个词，添加后文字格子置灰且不返回。
- PowerShell UTF-8 中文输出配置。

本地构建使用 `E:\SDK\jbr17` 作为 JDK 17。Android Studio 正在导入项目时，不要额外下载 JDK；等待 IDE 同步完成后再运行 Gradle 测试和 Debug 构建。

## 测试推送 App

工程包含一个独立模块 `:push-tester`，安装后 App 名称为 `推送测试器`，包名为 `com.hugo.pushtester`。它用于发送真实通知，验证主 App 是否能监听、匹配、拦截并写入历史。

构建命令：

```powershell
$env:JAVA_HOME='E:\SDK\jbr17'
.\gradlew.bat :push-tester:assembleDebug
```

使用方式：

1. 安装主 App `:app`，并在系统设置里开启它的通知使用权。
2. 安装 `:push-tester`，允许发送通知。
3. 点击“营销推送”，预期主 App 拦截并在历史中显示命中词。
4. 点击“物流正常推送”或“安全提醒”，预期默认放行。
5. 使用自定义推送输入标题和正文，验证手动黑白名单效果。
