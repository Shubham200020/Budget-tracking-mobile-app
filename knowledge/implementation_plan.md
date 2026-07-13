# Implementation Plan: Analytics & Charts Screen

## Problem
Currently there is **no analytics screen** in KhataBook. The app has raw data in Room DB but no way to visualize it. Every top competitor (Money Manager ⭐4.6, Monefy ⭐4.5, YNAB ⭐4.8) has charts as their #1 retention feature.

---

## Root Cause of "Not Working"
Charts don't exist yet — we need to:
1. Add a charting library (Vico 3.x — the standard for Jetpack Compose)
2. Build an `AnalyticsScreen.kt` with Pie + Bar charts
3. Wire it into the Navigation so users can reach it
4. Add a Bottom Navigation Bar (required once we have 3+ screens)

---

## Chosen Library: Vico 3.2.3
> **Why Vico?** It is the only chart library built **natively for Jetpack Compose** with Material 3 theming support. It's actively maintained, has a clean API, and works offline. Alternatives like MPAndroidChart are View-based (outdated), and there is no official Google Charts library for Compose.

---

## What We Will Build

### Screen 1 — Analytics Screen
#### Chart A: Pie Chart (Category Spending — Current Month)
- Shows percentage breakdown of spending by category
- Each slice is colored using the category's `colorHex`
- Tapping a slice highlights it and shows the exact amount + percentage

#### Chart B: Bar Chart (6-Month Spending Trend)
- X-axis: Last 6 calendar months (e.g., Feb → Jul)
- Y-axis: Total spent (₹)
- Two grouped bars per month: **Coral Red = Expenses**, **Emerald Green = Income**
- Allows users to see their spending trajectory at a glance

### Screen 2 — Bottom Navigation Bar
Required to make the Analytics screen reachable. 3 tabs:
| Tab | Icon | Screen |
|:---|:---|:---|
| 🏠 Home | `Home` icon | `DashboardScreen` |
| 📊 Analytics | `BarChart` icon | `AnalyticsScreen` |
| ⚙️ Settings | `Settings` icon | `SettingsScreen` (placeholder) |

---

## Proposed Code Changes

### Step 1: Add Vico to `libs.versions.toml`
```toml
[versions]
vico = "3.2.3"

[libraries]
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
```

### Step 2: Add Vico to `build.gradle.kts`
```kotlin
implementation(libs.vico.compose.m3)
```

### Step 3: Add 6-month query to `FinanceRepository`
- New function: `getExpensesBetweenDates(start, end)` already exists ✅
- New function needed: `getTotalSpentBetweenDates` already exists ✅
- We need to call it **6 times** (once per month) inside ViewModel

### Step 4: Update `FinanceViewModel.kt`
- Add `sixMonthBarData: StateFlow<List<Pair<String, Pair<Double,Double>>>>` — each entry is `(monthLabel, (totalIncome, totalSpent))`
- Add `categoryPieData: StateFlow<Map<Category, Double>>` — maps each category to total spent this month

### Step 5: Create `AnalyticsScreen.kt`
- `PieChartHost` with category data
- `CartesianChartHost` (Bar Chart) with 6-month income vs expense data
- Month selector at the top (reuses existing navigation state)

### Step 6: Update `MainActivity.kt`
- Replace the simple `NavHost` with a `Scaffold` that has a `NavigationBar` at the bottom
- Add 3 navigation items: Dashboard, Analytics, Settings

---

## Verification Plan
1. Run `./gradlew assembleDebug` — must succeed
2. Add 3 expenses to "Food", 2 to "Transport" → open Analytics → verify Pie chart shows 2 slices with correct proportions
3. Add expenses across 2–3 months → verify Bar Chart shows bars for each month
4. Tap each tab in Bottom Nav → verify screens switch correctly

> [!IMPORTANT]
> The Pie Chart in Vico 3.x uses a different API (`PieChartHost`) compared to the Cartesian charts. We will implement both types correctly using the latest 3.2.3 API patterns.

> [!TIP]
> Since we have no existing chart code to break, this is a clean, additive implementation — zero risk of regressions in the existing Dashboard or AddExpense screens.
