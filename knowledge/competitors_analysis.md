# Competitor Feature Comparison: Budgets & Monthly Tracking

To build a robust month-wise tracking system for KhataBook, we analyzed how top competitors handle time-bound data and budget editing.

---

## 🔍 Feature Comparison: Month-wise Tracking & Budgets

| Feature / App | Money Manager | YNAB (You Need A Budget) | Wallet by BudgetBakers | Goodbudget |
| :--- | :--- | :--- | :--- | :--- |
| **Time Navigation** | Left/Right swipe or arrows (`< July 2026 >`) at the top of the screen. | Month selector at the top. Focuses heavily on the *current* and *next* month. | Uses a global date filter (This Month, Last Month, Custom Range). | Envelope view per pay period or month. |
| **Budget Editing** | Set a static budget per category. Applies indefinitely unless changed. | "Zero-based" budgeting. User manually allocates every earned dollar into categories every month. | Set dynamic budgets for specific periods (One-time, Weekly, Monthly). | "Fill Envelopes" feature. You add money to category envelopes at the start of the month. |
| **Carry-over Balance** | Treats each month discretely. Unspent money does not explicitly carry over into the next month's budget visually. | Core philosophy: Positive/Negative balances roll over. If you overspend in June, July starts negative. | Optional feature. Users can choose if a budget rolls over. | Balances roll over. |
| **Expense Filtering** | The entire dashboard strictly filters by the selected month. | Shows activity strictly for the selected month's budget. | Dashboard widgets adapt to the global date filter. | Shows transactions within the envelope's period. |

---

## 🚀 Takeaways for KhataBook Implementation

Based on the analysis, a simple yet powerful budgeting app must have:

1. **Discrete Monthly Navigation:** Like *Money Manager*, KhataBook should have an intuitive `< July 2026 >` header on the Dashboard. Users should be able to look back at past months effortlessly.
2. **Context-Aware Filtering:** When the user changes the month to "June", the circular progress chart, the total spent, and the recent transactions list should *all* instantly update to show June's data.
3. **Flexible Budgeting:** Rather than YNAB's strict roll-over system (which has a steep learning curve), we should adopt a simpler approach: The user sets an overall monthly budget. If they edit the budget while viewing "July", it sets the limit specifically for July.
