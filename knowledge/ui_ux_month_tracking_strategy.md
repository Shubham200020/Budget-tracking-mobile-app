# UI/UX Strategy: Month-wise Tracking & Budget Editing

As requested, here is the detailed Senior UI/UX design plan for integrating month-wise filtering and budget editing directly into our existing Dashboard interface.

---

## 🎨 The Design Challenge
We need to introduce a way for the user to change the viewed time period (months) and edit their budget limit, without cluttering the sleek interface we've already built.

### 1. The Month Selector Component
**Placement:** We will place a minimalist navigation row immediately beneath the Top App Bar. 
**Design:** It will consist of two subtle `<` and `>` chevron arrows on the left and right, with bold, centered text displaying the current month and year (e.g., **July 2026**). 
**Interaction:** Tapping the arrows causes an instant reload of the circular progress chart and the recent transactions list, shifting the context to that specific month. No page loads, just smooth, instant re-rendering.

### 2. The Budget Edit Dialog
**Placement:** Instead of burying the budget settings in a separate "Settings" page, we want it to be contextually accessible.
**Design:** We will add a small, subtle edit (pencil) icon next to the "of ₹10,000" text under the main circular progress bar. 
**Interaction:** Tapping this icon instantly dims the background and overlays a minimalist `AlertDialog` containing:
1. A descriptive title: *"Set Budget for July 2026"*
2. A numeric input field utilizing our custom Surface Dark background.
3. A prominent **Emerald Green** "Save" button to commit the change.

---

## 🖼️ High-Fidelity Mockup

Here is the generated mockup illustrating the newly designed Dashboard with the Month Selector and the Budget Edit Dialog overlaid.

![Month Selector & Budget Edit Dialog Mockup](file:///C:/Users/shibh/.gemini/antigravity/brain/3ed8dc37-54bd-42d3-b53c-1eeafbb272e2/dashboard_month_selector_mockup_1783900013015.png)

> [!TIP]
> This design maintains our strict color hierarchy. The edit dialog leverages the sleek dark mode aesthetic without introducing new jarring colors, keeping the user focused on the financial data.
