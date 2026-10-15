package com.tuempresa.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tuempresa.inventario.ui.customer.CustomerScreen
import com.tuempresa.inventario.ui.navigation.AppScreen
import com.tuempresa.inventario.ui.product.ProductScreen
import com.tuempresa.inventario.ui.product.ProductViewModel
import com.tuempresa.inventario.ui.report.ReportScreen
import com.tuempresa.inventario.ui.sale.SaleScreen

class MainActivity : ComponentActivity() {
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ProductViewModel) {
    val navController = rememberNavController()
    val products by viewModel.allProducts.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val sales by viewModel.allSales.collectAsState()

    val items = listOf(
        AppScreen.Products to Icons.Default.Home,
        AppScreen.Customers to Icons.Default.Person,
        AppScreen.Sales to Icons.Default.ShoppingCart,
        AppScreen.Reports to Icons.Default.List
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { (screen, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Products.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreen.Products.route) {
                ProductScreen(viewModel = viewModel)
            }
            composable(AppScreen.Customers.route) {
                CustomerScreen(
                    customers = customers,
                    onAddCustomer = { name, phone, address ->
                        viewModel.insertCustomer(name, phone, address)
                    },
                    onDeleteCustomer = { customer ->
                        viewModel.deleteCustomer(customer)
                    }
                )
            }
            composable(AppScreen.Sales.route) {
                SaleScreen(
                    sales = sales,
                    customers = customers,
                    products = products,
                    onAddSale = { customer, product, qty, type, callback ->
                        viewModel.insertSale(customer, product, qty, type, callback)
                    }
                )
            }
            composable(AppScreen.Reports.route) {
                ReportScreen()
            }
        }
    }
}
