# 🔍 UI/UX & Codebase Audit Report: KhataBook
**Prepared by:** Senior Mobile Developer (10+ Years Experience)

---

## 1. Executive Summary & Current State
The KhataBook application has transitioned from a basic template to a highly functional, offline-first personal finance app. The architectural foundation (MVVM, Room ORM, Jetpack Compose, Kotlin Flow) is robust and follows modern Android development best practices.

### 🌟 Key Achievements
* **Premium Aesthetics:** Enforced dark mode (`#121212`) combined with Emerald Green (`#00E676`) and Coral Red (`#FF5252`) provides a sleek, modern UI comparable to top-tier financial apps like *YNAB* and *PocketGuard*.
* **Custom Charts:** Canvas-based custom Pie and Bar charts avoid heavy third-party library dependencies, keeping the APK light and rendering speed high.
* **Month-wise Isolation:** Monthly scoping of budgets, transactions, and category metrics correctly matches competitor patterns.

---

## 2. UI/UX Audit & Required Refinements

### ⚠️ Issue 1: Soft Keyboard vs. Custom Numpad Overlap (High Severity)
* **Description:** On the `AddExpenseScreen`, clicking the **Note (optional)** text field opens the Android system keyboard. This keyboard clashes with the custom graphic numpad positioned below it, compressing the screen layout and potentially hiding key inputs.
* **UX Solution:** Detect when the software keyboard (IME) is active. When active, **hide the custom numpad** and expand the scrollable area so the user can easily type their note and tap the "Save" button.

### ⚠️ Issue 2: Lack of Visual Category Signposts (Medium Severity)
* **Description:** The recent transactions list shows category names (e.g., "Food", "Salary") but lacks icons. Visual icons help users scan and categorize their transactions instantly without reading text.
* **UX Solution:** Map each default category name to a corresponding Material icon (e.g., Food ➔ `Restaurant`, Salary ➔ `MonetizationOn`) and display it in a circular avatar next to the transaction details.

### ⚠️ Issue 3: Text Formats & Localisation (Low Severity)
* **Description:** Currency formats use hardcoded `₹` and raw string conversions (e.g., `String.format("%.0f", amount)`). While functional, this doesn't format numbers using standard Indian currency notation (e.g., `₹1,50,000` instead of `₹150000`).
* **UX Solution:** Implement a centralized currency formatting utility using `NumberFormat` initialized with the `"en", "IN"` locale to support correct digit grouping (lakhs/crores).

---

## 3. Codebase & Architectural Recommendations

### 🔧 Room DB Indexing & Integrity
* **Indexes:** Ensure all foreign key columns are properly indexed in SQLite. The `categoryId` column in the `expenses` table is currently indexed ✅.
* **Cascading:** The foreign key in `Expense.kt` uses `onDelete = ForeignKey.SET_DEFAULT`. Ensure that there is a default category defined or fallback handling in case a category is deleted.

### 🔧 StateFlow Subscription Sharing
* In `FinanceViewModel.kt`, we use:
  `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)`
  This is the correct industry standard. The 5-second delay ensures configuration changes (like screen rotation) do not trigger resource reallocation.

---

## 4. Feature Gap & Competitor Differentiation

### 🚀 Phase 2.5: UPI/Bank SMS Parsing (The Indian Market Winner)
* Indian users rely heavily on UPI (GPay, PhonePe, Paytm). Auto-reading the SMS notification to draft an expense is the single biggest differentiator of **axio** and **Moneyview**.
* **Recommendation:** Add an optional permission request. When granted, use a BroadcastReceiver to listen for incoming transaction SMS messages from common banks (e.g., HDFC, SBI, ICICI) and display a "Pending Transaction Found" card on the Dashboard for one-tap logging.

### 🚀 Phase 3.1: CSV & PDF Export
* Power users want spreadsheets. We should implement a basic CSV generation utility that exports the current month's transaction list and launches the Android Share sheet.

---

## 5. Gradle & Packaging Debugging (July 13, 2026)

### ⚠️ Issue: Transitive Downgrade Clash (Downgrading material3 implementation)
* **Symptom:** Application crashed immediately on start with `java.lang.NoSuchMethodError: No static method SwipeToDismissBox...`
* **Root Cause:** Gradle's conflict resolution Downgraded Compose's multiplatform metadata `material3` to version `1.2.1` but left the Android-specific implementation `material3-android` resolving to `1.3.0`. The mismatch in compiled signatures vs runtime bytecode caused the crash.
* **Resolution:** Forced both `androidx.compose.material3:material3` and `androidx.compose.material3:material3-android` to version `1.2.1` inside `configurations.all` `resolutionStrategy` in `build.gradle.kts`. This aligned the dependencies perfectly and resolved the startup crash.

