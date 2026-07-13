# 🛠️ Troubleshooting Guide: Transitive Compose Mismatches & NoSuchMethodError

## 1. The Error Details

### ⚠️ Symptom
The application compiles successfully but crashes immediately upon launch (crash loop on startup) with the following error in ADB Logcat:

```text
FATAL EXCEPTION: main
Process: com.example.skmaccount, PID: 28276
java.lang.NoSuchMethodError: No static method SwipeToDismissBox(Landroidx/compose/material3/SwipeToDismissBoxState;Lkotlin/jvm/functions/Function3;Landroidx/compose/ui/Modifier;ZZLkotlin/jvm/functions/Function3;Landroidx/compose/runtime/Composer;II)V in class Landroidx/compose/material3/SwipeToDismissBoxKt; or its super classes
	at com.example.skmaccount.ui.screens.DashboardScreenKt.SwipeableTransactionItem(DashboardScreen.kt:266)
```

---

## 2. Root Cause Analysis

### The Version Split Mismatch
Jetpack Compose 1.6+ split many of its core UI artifacts into multiplatform metadata modules and platform-specific platform modules:
* `androidx.compose.material3:material3` (metadata API declarations)
* `androidx.compose.material3:material3-android` (Android JVM implementation bytecode)

When a new transitive dependency (like the Vico chart library `com.patrykandpatrick.vico:compose-m3`) was introduced, it was compiled against a newer pre-release Compose version (e.g., `1.3.0`). 

During conflict resolution, Gradle's resolution rules resolved:
* `material3` ➔ **`1.2.1`** (forced by the BOM or version catalog)
* `material3-android` ➔ **`1.3.0`** (transitive requirement from Vico, which was not explicitly locked)

This split mismatch meant that the compiler compiled `SwipeToDismissBox` against the signature of version `1.2.1`, but at runtime, the device loaded the implementation from `1.3.0` which had a different signature (or package configuration), resulting in a runtime `NoSuchMethodError`.

---

## 3. How to Resolve This Error

Force the compiler to align all Compose Material 3 platform-specific artifacts by appending a custom `resolutionStrategy` to `build.gradle.kts`:

```kotlin
configurations.all {
    resolutionStrategy {
        force("androidx.compose.material3:material3:1.2.1")
        force("androidx.compose.material3:material3-android:1.2.1")
    }
}
```

---

## 4. How to Prevent This in the Future (Best Practices)

### 📌 Rule 1: Always Align split-platform dependencies
Whenever a dependency crash like `NoSuchMethodError` occurs with a Jetpack Compose library, inspect the Gradle dependency tree using:
```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep "material3"
```
Look for any instances where the library name (e.g., `material3`) and the android-specific target (e.g., `material3-android`) have resolved to different versions. Force them both to match in the `resolutionStrategy`.

### 📌 Rule 2: Declare third-party library versions explicitly in the Catalog
Instead of adding arbitrary unversioned strings like `implementation("androidx.compose.material:material-icons-extended")` inside the dependencies block, add them to `gradle/libs.versions.toml` and reference them using `libs.name` syntax. This ensures Gradle can perform unified dependency resolution.

### 📌 Rule 3: Use Gradle Dependency constraints
For complex multi-module projects, define constraints in the root project's build file to prevent modules from drawing in mismatched versions:
```kotlin
dependencies {
    constraints {
        implementation("androidx.compose.material3:material3:1.2.1")
        implementation("androidx.compose.material3:material3-android:1.2.1")
    }
}
```
