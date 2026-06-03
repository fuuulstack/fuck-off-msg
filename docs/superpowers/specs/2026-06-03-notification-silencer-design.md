# Android Notification Silencer Design

## Goal

Build an Android app that listens to push notifications, matches notification text against user and default keyword rules, suppresses unwanted marketing notifications where the platform allows it, and records a complete notification history for later review.

The first release targets normal non-root Android devices. It uses Android notification listener access to read, classify, cancel, and record notifications. Because normal apps cannot always prevent the first instant of a notification sound or vibration before the system posts it, the MVP should describe suppression as best-effort cancellation and history-based rule training. The architecture should leave room for a later Shizuku/ADB-enhanced mode.

## Product Principles

- System-level notifications are always allowed first.
- Whitelist rules always beat blacklist and marketing rules.
- Every blocked notification explains why it was blocked.
- Users can train rules from real notification history with minimal typing.
- Notification history remains useful even when no notification is blocked.
- Default rules should start conservatively and offer a stronger optional mode.

## MVP Permission Model

The MVP uses:

- `NotificationListenerService` to receive notifications.
- Notification listener permission onboarding.
- Local storage for history and rules.
- App metadata lookup for app names and icons.

The MVP does not require:

- Root.
- Shizuku.
- ADB setup.
- Cloud classification.
- System app privileges.

Future Shizuku-enhanced mode may add stronger notification channel or app notification policy controls, but it is outside the MVP.

## Rule Priority

Rules are evaluated in this fixed order:

1. System whitelist.
2. User whitelist.
3. User blacklist.
4. Default conservative marketing rules.
5. Optional enhanced marketing rules.

If a notification matches a higher-priority allow rule, it is allowed even if it also contains marketing keywords. This prevents false positives such as an order shipment notification that also contains a coupon phrase.

## Rule Types

### System Whitelist

System-level notifications are always allowed. The system whitelist should include critical packages and categories such as:

- Android system and System UI.
- Phone and call notifications.
- SMS or default messaging app critical notifications.
- Alarm and calendar reminders.
- Payment, security, and device protection notifications when detectable.

The exact package list should be editable in code for the MVP and surfaced read-only in the UI.

### User Whitelist

Whitelist entries allow matching notifications to pass. Entries can be:

- Global keyword whitelist.
- Per-app keyword whitelist.
- Whole-app whitelist.

When a user adds a whitelist keyword from the smear selection screen, the app asks whether to add it globally or only for the current app.

### User Blacklist

Blacklist entries block matching notifications. Entries can be:

- Global keyword blacklist.
- Per-app keyword blacklist.
- Whole-app blacklist.

When a user adds a blacklist keyword from the smear selection screen, the app asks whether to add it globally or only for the current app.

### Default Conservative Marketing Rules

These are enabled by default and should target clearly promotional phrases:

- 优惠券
- 领券
- 红包
- 限时秒杀
- 秒杀
- 立减
- 满减
- 免费领取
- 专属福利
- 新人礼包
- 会员专享
- 特价
- 抢购
- 大促
- 清仓
- 爆款
- 折扣
- 返现
- 0元
- 低至
- 到手价

### Enhanced Marketing Rules

These are disabled by default. Users can enable them when they prefer stronger filtering and accept a higher false-positive risk:

- 活动
- 上新
- 推荐
- 热卖
- 福利
- 惊喜
- 邀你
- 错过再等
- 今日份
- 为你精选
- 限量
- 开抢
- 回馈
- 解锁
- 逛逛
- 看看
- 订阅
- 直播中
- 任务奖励

In the MVP, enhanced rules block on a single keyword match after the user enables the enhanced switch. The UI should label this as more aggressive.

## Notification Decision Flow

For every notification:

1. Extract package name, app name, app icon, title, text, expanded text, post time, notification key, and group key.
2. Normalize searchable text by combining title and body fields.
3. Check system whitelist. If matched, record as `system_allowed`.
4. Check user whitelist. If matched, record as `whitelist_allowed`.
5. Check user blacklist. If matched, cancel the notification and record as `blocked`.
6. Check conservative default rules. If matched, cancel and record as `blocked`.
7. Check enhanced default rules only if the user enabled them. If matched, cancel and record as `blocked`.
8. Otherwise record as `allowed`.

Blocked records must store the matched keyword and rule source. Allowed records should also store allow reasons when applicable.

## History Data Model

Each notification history record stores:

- Stable local ID.
- Package name.
- App display name.
- App icon reference or cached icon.
- Notification title.
- Notification body.
- Expanded text when available.
- Received time.
- Decision result: `allowed`, `blocked`, `system_allowed`, or `whitelist_allowed`.
- Matched keyword, when any.
- Matched rule type.
- Matched rule scope: global, per-app, system, conservative, or enhanced.
- Notification key for cancellation.
- Group key for grouped notification handling.
- Read state.
- Marked state.

The history table stores all notifications, not only blocked notifications.

## Main Navigation

The app uses four bottom navigation destinations:

- History.
- Apps.
- Rules.
- Settings.

## History Screen

History is the default home screen. It shows a flat chronological list by default.

Each row includes:

- App icon on the left.
- App name.
- Notification title.
- Body preview.
- Time.
- Decision badge.
- Matched keyword badge for blocked notifications.

Example badges:

- `已拦截 · 命中：优惠券`
- `已拦截 · 命中：限时秒杀`
- `白名单放行 · 订单已发货`
- `系统放行`

Swipe actions:

- `标记`: opens the smear selection screen directly.
- `更多`: reserved in the MVP and does not expose additional actions yet.

## Apps Screen

The Apps screen groups notifications by source app.

Each app row includes:

- App icon.
- App name.
- Total notification count.
- Blocked notification count.
- Recent matched keyword, when any.
- Current allow/block state, when any.

An app detail screen includes:

- That app's notification history.
- Keywords that were matched for this app.
- Per-app whitelist rules.
- Per-app blacklist rules.
- Whole-app whitelist or blacklist controls.

## Rules Screen

The Rules screen manages:

- Conservative default rules.
- Enhanced marketing rule switch.
- Global whitelist.
- Global blacklist.
- Per-app rule entry points.

Users can add and delete manual rules. Default rules can be enabled or disabled by group, but individual default keywords do not need per-keyword editing in the MVP.

## Settings Screen

Settings includes:

- Notification listener permission status and setup action.
- Explanation of best-effort suppression limits on normal Android devices.
- History retention setting.
- Disabled future entry for Shizuku-enhanced mode.

Import/export rules are outside the MVP.

## Smear Selection Screen

The smear selection screen is opened from a history item by swiping left and tapping `标记`.

Top area:

- Back button.
- Title: `涂抹选择文字`.
- Source app icon and app name.

Content area:

- Notification title and body are split into selectable cells.
- Each Chinese character is one cell.
- Consecutive English letters and numbers are grouped as one cell, such as `618`, `5.5`, or `VIP`.
- Punctuation can be shown as cells but should not be required for rule creation.
- The user can drag across cells to select text.
- Tapping an already selected cell toggles it off.
- Selected cells are highlighted.
- Cells belonging to already-added words are grayed out.

Selection semantics:

- A continuous selected range becomes one keyword.
- Non-continuous selected ranges become multiple keywords.
- Example: selecting `优惠券` creates one keyword.
- Example: selecting `优惠券` and `限时秒杀` creates two keywords.

Bottom action area:

- `添加白名单`
- `添加黑名单`

When adding to whitelist:

- Show a scope dialog with `添加为全局白名单` and `仅当前 App 放行`.
- Add all current selected ranges according to the selected scope.
- Gray out the cells used by the newly added words.
- Stay on the smear selection screen for additional selection.

When adding to blacklist:

- Show a scope dialog with `添加为全局黑名单` and `仅当前 App 拦截`.
- Add all current selected ranges according to the selected scope.
- Gray out the cells used by the newly added words.
- Stay on the smear selection screen for additional selection.

The MVP does not support reversing a just-added rule directly from the smear screen. Users can edit or delete rules from the Rules screen.

## Error Handling

- If notification listener permission is missing, show onboarding and keep history empty until permission is granted.
- If app icon lookup fails, show a generic app icon.
- If notification text is empty, still record package, app name, and time.
- If cancellation fails, record the notification and mark it as `blocked_attempted` internally, but display a clear blocked status only when cancellation succeeds.
- If a duplicate notification key arrives, update the existing record when appropriate instead of creating misleading duplicates.

## Testing Strategy

Unit tests:

- Rule priority evaluation.
- Whitelist over blacklist behavior.
- System whitelist behavior.
- Conservative and enhanced keyword matching.
- Continuous and non-continuous smear selection grouping.

Integration tests:

- Notification listener event to history record.
- Blocked notification stores matched keyword.
- User-created whitelist rule prevents default keyword blocking.
- Per-app rule affects only that app.

UI tests:

- History list displays app icon, badge, and matched keyword.
- Swipe `标记` opens smear selection.
- Smear selection creates one keyword for continuous ranges.
- Smear selection creates multiple keywords for non-continuous ranges.
- Add whitelist scope dialog appears.
- Add blacklist scope dialog appears.
- Added cells gray out and the screen does not close.

## Out Of Scope For MVP

- Root or system-level interception.
- Shizuku-enhanced controls.
- Cloud rule updates.
- Machine learning classification.
- Import/export of rules.
- Regex rule editor.
- Functional `更多` menu actions.
- Per-keyword editing of built-in default rule groups.

## Open Implementation Notes

- Kotlin is the preferred Android language.
- Jetpack Compose is preferred for UI because the smear selection grid benefits from declarative state.
- Room is preferred for local history and rule storage.
- Work should begin with a minimal Android project scaffold, rule engine tests, and a fake notification source for local UI verification before wiring the real notification listener.
