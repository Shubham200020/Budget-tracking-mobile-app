# 🐞 Error Logs & Prevention Strategies

This document logs recent errors encountered during development of **skmBudgex** and outlines how to prevent or resolve them in the future.

---

## 1. Jetpack Compose Layout Alignment Break
**Description:**
When replacing the circular graph's code block inside a `LazyColumn`, the property `horizontalAlignment = Alignment.CenterHorizontally` was accidentally omitted.
**Symptom:**
The entire dashboard graph and UI shifted to the left edge of the screen instead of being perfectly centered.
**Prevention/Resolution:**
- Always perform careful diff comparisons when replacing multi-line Jetpack Compose code blocks. 
- Ensure container properties (`padding`, `horizontalAlignment`, `verticalArrangement`) are preserved when updating the internal contents of a `Column` or `LazyColumn`.

## 2. KSP Caching Exception (`java.nio.file.NoSuchFileException`)
**Description:**
The build failed during the `:app:kspDebugKotlin` task with the error:
`e: [ksp] java.nio.file.NoSuchFileException: ...\kspCaches\debug\backups`
**Symptom:**
Gradle throws a fatal exception preventing compilation, often caused by out-of-sync or corrupted Kotlin Symbol Processing (KSP) cache files after Room database schema changes.
**Prevention/Resolution:**
- **Resolution:** Run a clean build using `.\gradlew.bat clean` to wipe the `build/` directory and force KSP to regenerate from scratch.
- **Prevention:** If making rapid successive changes to Room entities or DAOs, periodically run clean builds or disable the configuration cache for that specific build using `--no-configuration-cache`.

## 3. ADB Deployment Failure (`INSTALL_FAILED_INVALID_APK`)
**Description:**
The device deployment failed with:
`com.android.ddmlib.InstallException: INSTALL_FAILED_INVALID_APK: Scanning Failed.: Package ... code is missing`
**Symptom:**
The new APK fails to install on the physical device because a previous partial or corrupted installation exists, or the OS package manager locked the file.
**Prevention/Resolution:**
- **Resolution:** Manually uninstall the corrupted app package from the device via ADB: `adb uninstall com.example.skmaccount`.
- **Prevention:** Avoid disconnecting the device during a Gradle deployment. If the app begins behaving erratically or fails to install, a complete uninstall is the safest and fastest way to restore a clean deployment state.

## 4. Jetpack Compose Transitive Version Mismatch (`NoSuchMethodError`)
**Description:**
The app crashed instantly upon launching the `OnboardingScreen` with the fatal exception:
`java.lang.NoSuchMethodError: No static method HorizontalPager-xYaah8o(...)V in class Landroidx/compose/foundation/pager/PagerKt;`
**Symptom:**
This occurs when a new dependency (like `androidx.biometric`) transitively pulls in a newer version of a Compose module (e.g., `foundation:1.7.4`), while another module (e.g., `material3:1.2.1`) remains strictly locked. The compiler and runtime environment fall out of sync on method signatures.
**Prevention/Resolution:**
- **Resolution:** Explicitly force the transitive dependencies to match the expected BOM or locked versions in `build.gradle.kts`. In this case, we added:
  ```kotlin
  force("androidx.compose.foundation:foundation:1.6.0")
  force("androidx.compose.foundation:foundation-android:1.6.0")
  ```
- **Prevention:** Whenever adding new libraries, monitor the dependency tree (`.\gradlew.bat :app:dependencies`) to catch unexpected version bumps in core UI components, and lock versions using `resolutionStrategy` in `build.gradle.kts`.
