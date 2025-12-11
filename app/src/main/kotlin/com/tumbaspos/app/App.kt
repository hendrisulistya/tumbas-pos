package com.tumbaspos.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tumbaspos.app.presentation.home.HomeScreen
import com.tumbaspos.app.presentation.sales.SalesScreen
import com.tumbaspos.app.presentation.warehouse.WarehouseScreen
import com.tumbaspos.app.presentation.backup.BackupScreen
import com.tumbaspos.app.presentation.activation.ActivationScreen
import com.tumbaspos.app.presentation.activation.PostActivationScreen
import com.tumbaspos.app.presentation.reporting.ReportingScreen
import com.tumbaspos.app.presentation.sales.SalesOrderScreen
import com.tumbaspos.app.presentation.settings.SettingsScreen
import com.tumbaspos.app.data.repository.SettingsRepository
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import com.tumbaspos.app.presentation.settings.printer.PrinterSettingsScreen
import com.tumbaspos.app.presentation.sales.SalesOrderDetailScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Login : Screen("login", "Login")
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Scan : Screen("scan", "Scan", Icons.Default.QrCodeScanner)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Cart : Screen("cart", "Cart")
    data object SalesOrder : Screen("sales_orders", "Sales Orders", Icons.Default.Receipt)
    data object Warehouse : Screen("warehouse", "Warehouse")
    data object Purchase : Screen("purchase", "Purchase")
    data object Reporting : Screen("reporting", "Reporting")
    data object Backup : Screen("backup", "Backup")
    data object Activation : Screen("activation", "Activation")
    data object PostActivation : Screen("post_activation", "Setup")
    data object RestoreStore : Screen("restore_store", "Restore Store")
    data object PrinterSettings : Screen("printer_settings", "Printer")
    data object StoreSettings : Screen("store_settings", "Store")
    data object SalesOrderDetail : Screen("sales_order_detail", "Order Details")
    data object Product : Screen("products", "Products")
    data object About : Screen("about", "About")
    data object EmployerManagement : Screen("employer_management", "Manage Employees")
    data object AuditLog : Screen("audit_log", "Audit Log")
}

@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            val navController = rememberNavController()
            val settingsRepository: SettingsRepository = koinInject()
            val authManager: com.tumbaspos.app.domain.manager.AuthenticationManager = koinInject()
            val isAuthenticated by authManager.currentEmployer.collectAsState()
            
            // Restore session on app start
            LaunchedEffect(Unit) {
                authManager.restoreSession()
            }
            
            // Determine start destination - always require login after activation
            val startDestination = if (!settingsRepository.isActivated()) {
                Screen.Activation.route
            } else {
                Screen.Login.route
            }
            
            // Redirect to login if user becomes unauthenticated
            LaunchedEffect(isAuthenticated) {
                if (settingsRepository.isActivated() && isAuthenticated == null) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            
            val bottomNavItems = listOf(Screen.Home, Screen.Scan, Screen.Settings)

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val showBottomBar = currentDestination?.route != Screen.Login.route &&
                                       currentDestination?.route != Screen.Activation.route &&
                                       currentDestination?.route != Screen.Cart.route &&
                                       currentDestination?.route != Screen.Warehouse.route &&
                                       currentDestination?.route != Screen.Purchase.route &&
                                       currentDestination?.route != Screen.Reporting.route &&
                                       currentDestination?.route != Screen.Backup.route
                    
                    if (showBottomBar) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .height(62.dp),
                            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                        ) {
                            bottomNavItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon!!, contentDescription = null) },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            androidx.compose.foundation.layout.PaddingValues(
                                start = innerPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                                top = innerPadding.calculateTopPadding(),
                                end = innerPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                                bottom = 0.dp
                            )
                        )
                ) {
                    composable(Screen.Login.route) {
                        com.tumbaspos.app.presentation.auth.LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToCart = {
                                navController.navigate(Screen.Cart.route)
                            }
                        )
                    }
                    
                    composable(Screen.Scan.route) {
                        com.tumbaspos.app.presentation.scan.ScanScreen(
                            onNavigateToCart = {
                                navController.navigate(Screen.Cart.route)
                            }
                        )
                    }
                    
                    composable(Screen.Settings.route) {
                    val employerViewModel: com.tumbaspos.app.presentation.employer.EmployerManagementViewModel = koinViewModel()
                    val employerUiState by employerViewModel.uiState.collectAsState()
                    val context = LocalContext.current
                    
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBackup = { navController.navigate(Screen.Backup.route) },
                        onNavigateToPrinter = { navController.navigate(Screen.PrinterSettings.route) },
                        onNavigateToStoreSettings = { navController.navigate(Screen.StoreSettings.route) },
                        onNavigateToSalesOrder = { navController.navigate(Screen.SalesOrder.route) },
                        onNavigateToWarehouse = { navController.navigate(Screen.Warehouse.route) },
                        onNavigateToPurchaseOrder = { navController.navigate(Screen.Purchase.route) },
                        onNavigateToReporting = { navController.navigate(Screen.Reporting.route) },
                        onNavigateToProduct = { navController.navigate(Screen.Product.route) },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) },
                        onNavigateToEmployers = { navController.navigate(Screen.EmployerManagement.route) },
                        onNavigateToAuditLog = { navController.navigate(Screen.AuditLog.route) },
                        onChangePinClick = { employerViewModel.onChangePinClick() },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    
                    // Change PIN Dialog
                    if (employerUiState.isChangePinDialogOpen) {
                        com.tumbaspos.app.presentation.settings.ChangePinDialog(
                            onDismiss = { employerViewModel.onDismissChangePinDialog() },
                            onConfirm = { oldPin, newPin ->
                                employerViewModel.changePin(oldPin, newPin)
                            }
                        )
                    }
                    
                    // Show success/error messages
                    LaunchedEffect(employerUiState.pinChangeSuccess, employerUiState.pinChangeError) {
                        if (employerUiState.pinChangeSuccess) {
                            // Show success message
                            android.widget.Toast.makeText(
                                context,
                                "PIN changed successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (employerUiState.pinChangeError != null) {
                            // Show error message
                            android.widget.Toast.makeText(
                                context,
                                employerUiState.pinChangeError,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                    
                    composable(Screen.PrinterSettings.route) {
                        PrinterSettingsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.StoreSettings.route) {
                        com.tumbaspos.app.presentation.settings.store.StoreSettingsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(
                        route = Screen.SalesOrderDetail.route + "/{orderId}",
                        arguments = listOf(navArgument("orderId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable
                        SalesOrderDetailScreen(
                            orderId = orderId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.Cart.route) {
                        SalesScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    


                    composable(Screen.SalesOrder.route) {
                        SalesOrderScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { orderId ->
                                navController.navigate(Screen.SalesOrderDetail.route + "/$orderId")
                            }
                        )
                    }
                    composable(Screen.Warehouse.route) {
                        WarehouseScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.Purchase.route) {
                        com.tumbaspos.app.presentation.purchase.PurchaseScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.Reporting.route) {
                        com.tumbaspos.app.presentation.reporting.ReportingScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.Backup.route) {
                        BackupScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.Activation.route) {
                        ActivationScreen(
                            onActivationSuccess = {
                                navController.navigate(Screen.PostActivation.route) {
                                    popUpTo(Screen.Activation.route) { inclusive = true }
                                }
                            },
                            onNavigateToRestore = {
                                navController.navigate(Screen.RestoreStore.route)
                            }
                        )
                    }
                    
                    composable(Screen.RestoreStore.route) {
                        com.tumbaspos.app.presentation.activation.RestoreStoreScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onRestoreComplete = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.RestoreStore.route) { inclusive = true }
                                    popUpTo(Screen.Activation.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable(Screen.PostActivation.route) {
                        com.tumbaspos.app.presentation.activation.PostActivationScreen(
                            onComplete = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.PostActivation.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Product.route) {
                        com.tumbaspos.app.presentation.product.ProductScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.About.route) {
                        com.tumbaspos.app.presentation.settings.AboutScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.EmployerManagement.route) {
                        com.tumbaspos.app.presentation.employer.EmployerManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                
                    composable(Screen.AuditLog.route) {
                        com.tumbaspos.app.presentation.audit.AuditLogScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}