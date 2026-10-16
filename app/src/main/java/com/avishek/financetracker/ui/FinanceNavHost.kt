package com.avishek.financetracker.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.avishek.financetracker.ui.add.AddEditScreen
import com.avishek.financetracker.ui.add.AddEditViewModel
import com.avishek.financetracker.ui.history.HistoryScreen
import com.avishek.financetracker.ui.overview.OverviewScreen
import com.avishek.financetracker.ui.settings.CategoriesScreen
import com.avishek.financetracker.ui.settings.SettingsScreen

private object Routes {
    const val OVERVIEW = "overview"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val CATEGORIES = "categories"
    const val EDIT = "edit?${AddEditViewModel.ARG_ID}={${AddEditViewModel.ARG_ID}}"

    fun edit(id: Long = AddEditViewModel.NEW_ID) = "edit?${AddEditViewModel.ARG_ID}=$id"
}

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Overview(Routes.OVERVIEW, "Overview", Icons.Filled.PieChart),
    History(Routes.HISTORY, "History", Icons.AutoMirrored.Filled.ReceiptLong),
    Settings(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
}

@Composable
fun FinanceNavHost(navController: NavHostController = rememberNavController()) {
    val snackbarHostState = remember { SnackbarHostState() }

    NavHost(navController = navController, startDestination = Routes.OVERVIEW) {
        composable(Routes.OVERVIEW) {
            TabScaffold(Tab.Overview, navController, snackbarHostState) { padding ->
                OverviewScreen(contentPadding = padding)
            }
        }
        composable(Routes.HISTORY) {
            TabScaffold(Tab.History, navController, snackbarHostState) { padding ->
                HistoryScreen(
                    contentPadding = padding,
                    snackbarHostState = snackbarHostState,
                    onEdit = { id -> navController.navigate(Routes.edit(id)) },
                )
            }
        }
        composable(Routes.SETTINGS) {
            TabScaffold(Tab.Settings, navController, snackbarHostState) { padding ->
                SettingsScreen(
                    contentPadding = padding,
                    snackbarHostState = snackbarHostState,
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                )
            }
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument(AddEditViewModel.ARG_ID) {
                    type = NavType.LongType
                    defaultValue = AddEditViewModel.NEW_ID
                },
            ),
        ) {
            AddEditScreen(onDone = { navController.popBackStack() })
        }
        composable(Routes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** The shared frame for the three main tabs: title bar, bottom navigation and the "+" button. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabScaffold(
    current: Tab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = { TopAppBar(title = { Text(current.label) }) },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (current != Tab.Settings) {
                FloatingActionButton(onClick = { navController.navigate(Routes.edit()) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add transaction")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content,
    )
}
