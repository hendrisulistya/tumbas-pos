package com.argminres.app.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Login : Screen("login", "Login")
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Cart : Screen("cart", "Cart")
    data object SalesOrder : Screen("sales_orders", "Sales Orders", Icons.Default.Receipt)
    data object Showcase : Screen("showcase", "Etalase")
    data object Purchase : Screen("purchase", "Purchase")
    data object Reporting : Screen("reporting", "Reporting")
    data object Backup : Screen("backup", "Backup")
    data object Activation : Screen("activation", "Activation")
    data object PostActivation : Screen("post_activation", "Setup")
    data object SessionCheck : Screen("session_check", "Session Check")
    data object RestoreStore : Screen("restore_store", "Restore Store")
    data object EndOfDay : Screen("end_of_day", "End of Day")
    data object WorkInProcess : Screen("work_in_process", "Work in Process")
    data object PrinterSettings : Screen("printer_settings", "Printer")
    data object StoreSettings : Screen("store_settings", "Store")
    data object SalesOrderDetail : Screen("sales_order_detail", "Order Details")
    data object About : Screen("about", "About")
    data object EmployerManagement : Screen("employer_management", "Manage Employees")
    data object AuditLog : Screen("audit_log", "Audit Log")
    data object Ingredient : Screen("ingredient", "Bahan")
    data object IngredientMaster : Screen("ingredient_master", "Kelola Bahan")
    data object DishMaster : Screen("dish_master", "Kelola Etalase")
}
