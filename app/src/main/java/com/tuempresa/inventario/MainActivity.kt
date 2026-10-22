package com.tuempresa.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.tuempresa.inventario.ui.credit.CreditPaymentScreen
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: ProductViewModel) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    val products by viewModel.allProducts.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val sales by viewModel.allSales.collectAsState()

    val items = listOf(
        Triple(AppScreen.Products, Icons.Default.Home, "Inventario"),
        Triple(AppScreen.Customers, Icons.Default.Person, "Clientes"),
        Triple(AppScreen.Sales, Icons.Default.ShoppingCart, "Ventas"),
        Triple(AppScreen.Reports, Icons.Default.List, "Reportes"),
        Triple(AppScreen.Reports, Icons.Default.ShoppingCart, "Créditos")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, (_, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> ProductScreen(viewModel = viewModel)
                1 -> CustomerScreen(viewModel = viewModel)
                2 -> SaleScreen(
                    sales = sales,
                    customers = customers,
                    products = products,
                    onAddSale = { customer, product, qty, type, callback ->
                        viewModel.insertSale(customer, product, qty, type, callback)
                    },
                    onDeleteSale = { sale ->
                        viewModel.deleteSaleAndRestoreStock(sale)
                    }
                )
                3 -> ReportScreen(sales = sales, customers = customers, products = products)
                4 -> CreditPaymentScreen(viewModel = viewModel)
            }
        }
    }
}
