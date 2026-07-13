package com.example.skmaccount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skmaccount.data.database.AppDatabase
import com.example.skmaccount.data.repository.FinanceRepository
import com.example.skmaccount.ui.screens.*
import com.example.skmaccount.ui.theme.SKMAccountTheme
import com.example.skmaccount.ui.viewmodel.FinanceViewModel
import com.example.skmaccount.ui.viewmodel.FinanceViewModelFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val android.content.Context.dataStore by preferencesDataStore(name = "settings")
private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

// Bottom nav destinations
sealed class BottomNav(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : BottomNav("dashboard", "Home", Icons.Filled.Home)
    object Analytics : BottomNav("analytics", "Analytics", Icons.Filled.Analytics)
    object Settings  : BottomNav("settings",  "Settings",  Icons.Filled.Settings)
}

val bottomNavItems = listOf(BottomNav.Dashboard, BottomNav.Analytics, BottomNav.Settings)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

                when (onboardingDone) {
                    null -> { /* Loading — wait for DataStore */ }
                    false -> {
                        OnboardingScreen(onFinish = {
                            scope.launch { dataStore.edit { it[ONBOARDING_DONE] = true } }
                        })
                    }
                    true -> KhataBookApp(repository)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataBookApp(repository: FinanceRepository) {
    val navController = rememberNavController()
    val viewModel: FinanceViewModel = viewModel(factory = FinanceViewModelFactory(repository))
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Routes that show the Bottom Navigation Bar
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
                SettingsScreen()
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