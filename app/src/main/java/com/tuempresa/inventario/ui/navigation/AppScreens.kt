package com.tuempresa.inventario.ui.navigation

sealed class AppScreen(val route: String, val title: String) {
    object Products : AppScreen("products", "Inventario")
    object Customers : AppScreen("customers", "Clientes")
    object Sales : AppScreen("sales", "Ventas")
    object Reports : AppScreen("reports", "Reportes")
}
