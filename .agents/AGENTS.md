# Project Knowledge: KhataBook / SKMACCOUNT

## Codebase Audit Findings (As of July 13, 2026)
* The project `KhataBook` (root project name `SKMACCOUNT`) is currently an empty boilerplate Android template.
* **MainActivity**: `com.example.skmaccount.MainActivity` is a default activity with no custom business logic.
* **Layout**: `activity_main.xml` displays a simple `"Hello World!"` text.
* **Build Configuration**: Configured with Kotlin Gradle DSL (`build.gradle.kts` and `settings.gradle.kts`).
* **Expense & Budget Systems**: None currently exist. Implementing these will require adding Room database schemas, user models, categories, UI fragments or composables, and logic layers.

## SQLite Architecture Implementation (Completed July 13, 2026)
* **Tech Stack**: Android Room ORM, Kotlin Coroutines, Jetpack Compose, KSP (Kotlin Symbol Processing).
* **Data Models**: Created `Category` (with color/icon), `Expense` (linked to Category), and `Budget` (monthly limits).
* **Data Access Objects (DAOs)**: Created `CategoryDao`, `ExpenseDao`, and `BudgetDao` with synchronous write operations to avoid KSP `unexpected JVM signature V` issues on Room 2.6.1.
* **Database**: `AppDatabase` is implemented with a Room DatabaseCallback to seed default categories (Food, Transport, Shopping, Utilities) upon first creation.
* **Repository**: `FinanceRepository` encapsulates all DAO calls, exposing `Flow` for reads and wrapping writes with `withContext(Dispatchers.IO)` to ensure thread safety.
* **Build Configuration**: Enabled Jetpack Compose and successfully integrated Room dependencies in `build.gradle.kts` while mitigating KSP & Kotlin 2.0.0 built-in plugin source-set conflicts.

## UI/UX Design Strategy
* **Persona**: Designed for the "Conscious Spender" who needs immediate visual feedback on their financial health.
* **Color Palette**: Dark Mode foundation (`#121212`) combined with **Emerald Green** (`#00E676`) for income/safe budgets and **Coral Red** (`#FF5252`) for expenses/over-budget warnings. This modernizes the typical "red/green" traffic light psychology without being visually aggressive.
* **Core Screens**: 
    1. **Dashboard**: Features a circular progress chart indicating budget consumption and a list of recent transactions.
    2. **Add Expense**: A sleek input screen featuring a large numpad and red/green highlighted inputs.
* **Tech Stack for UI**: Jetpack Compose using Material 3 guidelines and custom color tokens.

## Competitor Analysis & Feature Strategy (Budgets & Month-wise Tracking)
* **Time Navigation (Competitor Edge)**: Similar to top apps (Money Manager, YNAB), KhataBook uses discrete monthly tracking. The dashboard filters all expenses and budget data strictly to a selected month (e.g., `< July 2026 >`), avoiding complex roll-over math for the user.
* **Budget Setting**: Budgets are set on a per-month basis natively within the Room database. The user can edit the budget directly from the dashboard via an intuitive UI dialog, immediately reflecting the state for the actively viewed month.

## Senior Developer Feature Gap Analysis (July 13, 2026)
* **Implemented**: Manual expense entry, Category system, Monthly budget, Month navigation, Category breakdowns with horizontal progress bars, Category-level budgets.
* **Critical Missing (Phase 1)**: Income tracking (no net balance possible without it), Empty state UI (blank dashboard kills retention), Onboarding (3-slide first-run experience), Expense notes/description field, Swipe-to-delete transactions, Success Snackbar feedback.
* **Differentiators Missing (Phase 2)**: Bottom Navigation Bar (needed for 3+ screens), Analytics screen with Pie/Bar charts (#1 user-requested feature), App Lock via Biometric/PIN, Budget alert push notifications via WorkManager.
* **Power Features (Phase 3)**: Recurring expenses, Search & filter, CSV export, Google Drive backup.
* **India Market Notes**: SMS auto-read (axio-style) is a large competitive gap. Offline-first (Room SQLite) is correctly implemented. Hindi support unlocks tier-2 markets long-term.
* **DB Migration Note**: Income tracking (`isIncome: Boolean` on `Expense` entity) must be done FIRST before any real user data accumulates since it requires a Room migration.

## Mobile App Design Principles (Knowledge from Session)
* **User-Centered Design**: Build personas, keep every screen to one primary action, use Bottom Nav / FAB for frictionless navigation.
* **Visual Hierarchy (Thumb Zone)**: Critical interactive elements at the bottom of the screen, Typography minimum 16sp for body text.
* **Feedback & Micro-interactions**: Tap ripples, skeleton loaders for loading states, friendly Empty State illustrations with Call-to-Action.
* **Performance**: Smooth 60fps, Offline-first (Room SQLite ✅), graceful human-readable error handling.
* **Accessibility (a11y)**: Minimum 48×48dp touch targets, content descriptions on icons, sufficient contrast ratios.

## Competitor Intelligence: Full Feature Matrix (July 13, 2026)
### Competitor Ratings & Differentiators
* **Money Manager (Realbyte)** ⭐4.6 — Double-entry bookkeeping, extensive charts, dark mode, manual control.
* **YNAB** ⭐4.8 — Zero-based envelope budgeting, goal tracking, bank sync, strong education resources.
* **Wallet (BudgetBakers)** ⭐4.4 — Bank auto-sync, multi-currency, shared/group budgets.
* **Goodbudget** ⭐4.3 — Envelope budgeting, balance roll-over, household sharing.
* **axio / Moneyview** ⭐4.2 — SMS auto-read (India-specific), UPI tracking, bill reminders.
* **PocketGuard** ⭐4.3 — "Safe to spend" metric, simplicity-first.
* **Monefy** ⭐4.5 — One-screen chart-first UI, minimalist.

### UI/UX Pattern Findings
* **Navigation**: Bottom Navigation Bar with 4–5 tabs is the de-facto standard for financial apps. KhataBook currently has only 2 screens and no bottom nav — this must be added in Phase 2.
* **Dashboard Layout**: Large hero number + Circular progress (KhataBook ✅) + Category bars (KhataBook ✅) + Income vs Expense summary cards (KhataBook ❌).
* **Color patterns**: Green=safe, Red=danger (KhataBook ✅), Amber=warning (80% budget, KhataBook ❌), Blue=income (KhataBook ❌).
* **Micro-interactions missing**: Progress bar fill animation on screen entry, swipe-to-delete, pull-to-refresh, FAB scale animation, success snackbar after saving expense.

### Phase 1 — Critical Missing Features (Must-Have)
1. Income Tracking (`isIncome` field on Expense entity, requires Room migration)
2. Empty State UI on Dashboard when no transactions exist
3. Onboarding Screen (3-slide HorizontalPager, DataStore flag)
4. Transaction Notes/Description field on Expense
5. Swipe-to-Delete transactions (SwipeToDismissBox Material 3)
6. Success Snackbar after saving expense

### Phase 2 — Differentiator Features
7. Bottom Navigation Bar (Dashboard, Analytics, Settings)
8. Analytics Screen with Pie Chart + 6-month Bar Chart (vico library)
9. App Lock — Biometric/PIN (androidx.biometric)
10. Budget Alert Push Notifications at 80% and 100% (WorkManager)

### Phase 3 — Power Features
11. Recurring Expenses (auto-log fixed monthly costs)
12. Search & Filter on transaction history
13. CSV Export via Android Share sheet
14. Google Drive Cloud Backup

### India Market Intelligence
* SMS auto-read (axio-style) is the biggest competitive gap for Indian users.
* Offline-first (Room SQLite) is correctly implemented ✅ — critical for tier-2 cities.
* Hindi language support unlocks large new user base (future roadmap).
* Simplicity wins in India — YNAB-style complexity has low penetration.

## UI/UX & Formatting Refinements (Completed July 13, 2026)
* **Keyboard Overlap Fix**: Hides the custom numeric keypad on `AddExpenseScreen` when the soft keyboard is active (uses `WindowInsets.isImeVisible` with `ExperimentalLayoutApi`) and makes input text fields scrollable.
* **Category Avatars & Badges**: Uses a custom helper `CategoryHelper` to resolve and render distinct Material icons (e.g. `Restaurant`, `DirectionsCar`, `ShoppingCart`) for transaction items and category breakdown rows.
* **Localised Currency (en_IN)**: Centralised number formatting via `CategoryHelper.formatCurrency(Double)` using `Locale("en", "IN")` for proper lakhs/crores formatting (e.g. `₹1,50,000` instead of `₹150000`).
* **Visual Donut/Bar Charts**: Custom Compose Canvas-drawn analytics charts for category shares and 6-month historical income vs. expense comparison.

## Build & Dependency Fixes (July 13, 2026)
* **Compose Version Alignment**: Fixed a runtime startup crash (`NoSuchMethodError: No static method SwipeToDismissBox`) by forcing both `androidx.compose.material3:material3` and `androidx.compose.material3:material3-android` to `1.2.1` in the build gradle's configuration `resolutionStrategy`. This resolved a transient downgrade clash introduced by the charting library.
* **Room Fallback Migration**: Added `.fallbackToDestructiveMigration()` to the Room database builder to prevent crashes on schema upgrades during development.




