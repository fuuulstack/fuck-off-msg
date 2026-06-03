# Glass UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the Android Compose UI into a fresh gray-white glass style with icons for every major function.

**Architecture:** Keep the existing repository, navigation, rules, listener, and persistence behavior unchanged. Add a small presentation component layer for glass cards, icon labels, status pills, and app avatars, then update each screen to consume those components.

**Tech Stack:** Android Kotlin, Jetpack Compose Material 3, Compose Material Icons Extended, existing Gradle Android plugin.

---

## File Structure

- Modify `app/build.gradle.kts`: add Compose Material Icons Extended dependency.
- Modify `app/src/main/java/com/hugo/notificationsilencer/theme/Theme.kt`: define the gray-white color scheme, typography defaults, and glass constants.
- Create `app/src/main/java/com/hugo/notificationsilencer/ui/components/GlassComponents.kt`: shared glass surfaces, page headers, status pills, app avatars, and icon label buttons.
- Modify `app/src/main/java/com/hugo/notificationsilencer/navigation/AppNav.kt`: replace text-only bottom navigation with icon navigation and a glass-style container.
- Modify `app/src/main/java/com/hugo/notificationsilencer/ui/history/HistoryScreen.kt`: redesign history as dashboard stats plus glass notification cards.
- Modify `app/src/main/java/com/hugo/notificationsilencer/ui/apps/AppsScreen.kt`: redesign app summaries with icon avatars and stat pills.
- Modify `app/src/main/java/com/hugo/notificationsilencer/ui/rules/RulesScreen.kt`: redesign rule sections and keyword chips.
- Modify `app/src/main/java/com/hugo/notificationsilencer/ui/settings/SettingsScreen.kt`: redesign settings as icon-led glass rows.
- Modify `app/src/main/java/com/hugo/notificationsilencer/ui/mark/MarkScreen.kt`: redesign mark screen with source card, glass token cells, and bottom action bar.

## Tasks

### Task 1: Add UI Dependency And Theme

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/hugo/notificationsilencer/theme/Theme.kt`

- [ ] Add `implementation("androidx.compose.material:material-icons-extended")`.
- [ ] Replace the default color scheme with gray-white glass-oriented colors.
- [ ] Keep all theme changes visual only.
- [ ] Run `.\gradlew.bat :app:assembleDebug`.

### Task 2: Shared Glass Components

**Files:**
- Create: `app/src/main/java/com/hugo/notificationsilencer/ui/components/GlassComponents.kt`

- [ ] Add `GlassCard`, `PageHeader`, `StatusPill`, `AppAvatar`, `IconLabelButton`, and `GlassBackground`.
- [ ] Use 8dp-based spacing and restrained 8-16dp rounding.
- [ ] Use Material icons for function recognition.
- [ ] Run `.\gradlew.bat :app:assembleDebug`.

### Task 3: Navigation Redesign

**Files:**
- Modify: `app/src/main/java/com/hugo/notificationsilencer/navigation/AppNav.kt`

- [ ] Fix Chinese labels to `历史`, `应用`, `规则`, `设置`.
- [ ] Add semantic icons for each destination.
- [ ] Make the bottom navigation white/gray glass-like with clear selected state.
- [ ] Run `.\gradlew.bat :app:assembleDebug`.

### Task 4: Screen Redesign

**Files:**
- Modify: `app/src/main/java/com/hugo/notificationsilencer/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/hugo/notificationsilencer/ui/apps/AppsScreen.kt`
- Modify: `app/src/main/java/com/hugo/notificationsilencer/ui/rules/RulesScreen.kt`
- Modify: `app/src/main/java/com/hugo/notificationsilencer/ui/settings/SettingsScreen.kt`

- [ ] Update all visible Chinese text to readable Chinese.
- [ ] Add icons to history status, app stats, rule sections, and settings rows.
- [ ] Show blocked notification keyword as `命中：关键词`.
- [ ] Keep history default as a flat list.
- [ ] Run `.\gradlew.bat :app:assembleDebug`.

### Task 5: Mark Screen Redesign

**Files:**
- Modify: `app/src/main/java/com/hugo/notificationsilencer/ui/mark/MarkScreen.kt`

- [ ] Add glass source notification card.
- [ ] Restyle token cells: neutral, selected, and added states.
- [ ] Keep continuous and non-continuous selection behavior unchanged.
- [ ] Add icon buttons for whitelist and blacklist actions.
- [ ] Keep user on the screen after adding rules and gray out added tokens.
- [ ] Run `.\gradlew.bat :app:assembleDebug`.

### Task 6: Verification

**Files:**
- No code changes.

- [ ] Run `.\gradlew.bat testDebugUnitTest`.
- [ ] Run `.\gradlew.bat :app:assembleDebug :push-tester:assembleDebug`.
- [ ] Install app APK to the connected device with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- [ ] Launch the app with `adb shell am start -n com.hugo.notificationsilencer/.MainActivity`.
- [ ] Inspect UI on device for readable Chinese, icons, no obvious overlap, and gray-white glass visual style.
