package com.example.skmaccount

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skmaccount.data.database.AppDatabase
import com.example.skmaccount.data.repository.FinanceRepository
import com.example.skmaccount.ui.screens.*
import com.example.skmaccount.ui.theme.EmeraldGreen
import com.example.skmaccount.ui.theme.SKMAccountTheme
import com.example.skmaccount.ui.viewmodel.FinanceViewModel
import com.example.skmaccount.ui.viewmodel.FinanceViewModelFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

private val android.content.Context.dataStore by preferencesDataStore(name = "settings")
private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")

// Bottom nav destinations
sealed class BottomNav(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : BottomNav("dashboard", "Home", Icons.Filled.Home)
    object Analytics : BottomNav("analytics", "Analytics", Icons.Filled.Analytics)
    object Settings  : BottomNav("settings",  "Settings",  Icons.Filled.Settings)
}

val bottomNavItems = listOf(BottomNav.Dashboard, BottomNav.Analytics, BottomNav.Settings)

class MainActivity : FragmentActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        executor = androidx.core.content.ContextCompat.getMainExecutor(this)

        val database = AppDatabase.getDatabase(this)
        val repository = FinanceRepository(
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao()
        )

        setContent {
            SKMAccountTheme {
                val scope = rememberCoroutineScope()
                val onboardingDone by dataStore.data
                    .map { prefs -> prefs[ONBOARDING_DONE] ?: false }
                    .collectAsState(initial = null)
                val isAppLockEnabled by dataStore.data
                    .map { prefs -> prefs[APP_LOCK_ENABLED] ?: false }
                    .collectAsState(initial = null)

                var isAuthenticated by remember { mutableStateOf(false) }

                // The function to request biometric/PIN authentication
                val requireAuthentication: (onSuccess: () -> Unit) -> Unit = { onSuccess ->
                    biometricPrompt = BiometricPrompt(this@MainActivity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                            }
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                onSuccess()
                            }
                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                            }
                        })

                    promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock KhataBook")
                        .setSubtitle("Authenticate using your device credentials")
                        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                }

                if (onboardingDone == null || isAppLockEnabled == null) {
                    // Loading State
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                } else if (!onboardingDone!!) {
                    OnboardingScreen(onFinish = {
                        scope.launch { dataStore.edit { it[ONBOARDING_DONE] = true } }
                    })
                } else if (isAppLockEnabled!! && !isAuthenticated) {
                    // Lock Screen Overlay
                    LockScreen(onUnlockClicked = {
                        requireAuthentication { isAuthenticated = true }
                    })
                } else {
                    KhataBookApp(
                        repository = repository,
                        isAppLockEnabled = isAppLockEnabled!!,
                        onAppLockToggle = { enabled ->
                            scope.launch { dataStore.edit { it[APP_LOCK_ENABLED] = enabled } }
                        },
                        onRequireAuthentication = requireAuthentication
                    )
                }
            }
        }
    }
}

@Composable
fun LockScreen(onUnlockClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "App is Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onUnlockClicked,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Unlock KhataBook", color = androidx.compose.ui.graphics.Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataBookApp(
    repository: FinanceRepository,
    isAppLockEnabled: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
    onRequireAuthentication: (onSuccess: () -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: FinanceViewModel = viewModel(factory = FinanceViewModelFactory(repository))
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val bottomNavRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNav.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNav.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = { navController.navigate("add_expense") }
                )
            }
            composable(BottomNav.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
            composable(BottomNav.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    isAppLockEnabled = isAppLockEnabled,
                    onAppLockToggle = onAppLockToggle,
                    onRequireAuthentication = onRequireAuthentication
                )
            }
            composable("add_expense") {
                AddExpenseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}