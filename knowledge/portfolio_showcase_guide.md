# 🚀 Showcase Guide: How to Attach skmBudgex to Your Portfolio

To make **skmBudgex** stand out to potential employers, clients, or on platforms like LinkedIn, you should present it as a high-quality case study. Here is the recommended blueprint:

---

## 1. Optimize Your GitHub Repository
Your repository is your primary evidence. Make sure it looks clean and readable.

* **Repository link:** [https://github.com/Shubham200020/Budget-tracking-mobile-app](https://github.com/Shubham200020/Budget-tracking-mobile-app)
* **README Checklist:**
  - **Hero Header:** Display the **`skmBudgex`** name and its modern logo icon.
  - **Description:** *"A premium, offline-first personal finance and budget manager built in Kotlin and Jetpack Compose using Room SQLite and MVVM architecture."*
  - **Feature Bullet Points:** List key achievements (Onboarding, Income tracking, Custom Donut & Bar charts, Swipe-to-delete, Category budgets).
  - **Tech Stack Table:** Clearly highlight your stack (Compose, Coroutines Flow, Room ORM, KSP, DataStore, Material 3).
  - **Architecture Diagram:** Briefly outline how the View (Compose UI) binds to StateFlow inside the ViewModel, which fetches data from the Room FinanceRepository.

---

## 2. Record a 30-Second Demo Video
Recruiters rarely download and compile APK code; they prefer to **watch a short video**.

* **How to Record:**
  - Connect your device and run ADB to record the screen:
    ```bash
    adb shell screenrecord /sdcard/demo.mp4
    ```
  - Click through the app: Show the onboarding slides, tap "Get Started", add an income of ₹10,000, add a food expense of ₹2,500 with a note, view the donut chart reload, and swipe left to delete.
  - Pull the video from the device:
    ```bash
    adb pull /sdcard/demo.mp4 ./demo.mp4
    ```
* **Showcase:** Upload this to your portfolio site or embed it as a GIF directly in your GitHub README!

---

## 3. Create an Interactive Web Emulator
Let users test the app directly in their web browser!

* **Tool:** Use [Appetize.io](https://appetize.io/) (free tier available).
* **Steps:**
  1. Build your release or debug APK:
     ```bash
     ./gradlew assembleDebug
     ```
  2. Locate the APK file at `app/build/outputs/apk/debug/app-debug.apk`.
  3. Upload the APK to Appetize.io.
  4. Copy the iframe code and embed it directly into your portfolio website. Visitors will be able to click, type, and navigate your app live on your site!

---

## 4. Write a "Behind the Build" Case Study
Describe the technical challenges you solved to prove your senior-level engineering skills.

* **Highlight the Database Migration:** Explain how you updated the Room database schema from version 1 to 2 (adding notes and income tracking) using structured SQLite migration scripts without data loss.
* **Highlight custom Canvas drawing:** Explain why you chose custom Canvas drawing over heavy charting libraries to keep rendering performance high and reduce APK footprint.
* **Highlight dependency resolution:** Describe how you resolved split-dependency package mismatches (`material3` vs `material3-android` runtime mismatch) to prevent startup crashes.
