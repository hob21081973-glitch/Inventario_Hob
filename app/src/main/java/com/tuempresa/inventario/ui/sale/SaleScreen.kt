package com.tuempresa.inventario.ui.sale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuempresa.inventario.data.local.CustomerEntity
import com.tuempresa.inventario.data.local.ProductEntity
import com.tuempresa.inventario.data.local.SaleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
    sales: List<SaleEntity>,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    onAddSale: (String, String, Int, String, (Boolean, String) -> Unit) -> Unit
) {
    var customerQuery by remember { mutableStateOf("") }
    var expandedCustomer by remember { mutableStateOf(false) }

    var productQuery by remember { mutableStateOf("") }
    var expandedProduct by remember { mutableStateOf(false) }

    var selectedProductPrice by remember { mutableStateOf(0.0) }
    var quantityText by remember { mutableStateOf("1") }
    var isCredit by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // Filtrar clientes según lo que escribe el usuario
    val filteredCustomers = customers.filter { it.name.contains(customerQuery, ignoreCase = true) }
    // Filtrar productos según lo que escribe el usuario
    val filteredProducts = products.filter { it.name.contains(productQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Nueva Venta con Stock", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // --- FILTRO DE CLIENTES ---
        ExposedDropdownMenuBox(
            expanded = expandedCustomer && filteredCustomers.isNotEmpty(),
            onExpandedChange = { expandedCustomer = it }
        ) {
            OutlinedTextField(
                value = customerQuery,
                onValueChange = {
                    customerQuery = it
                    expandedCustomer = true
                },
                label = { Text("Buscar Cliente...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedCustomer && filteredCustomers.isNotEmpty(),
                onDismissRequest = { expandedCustomer = false }
            ) {
                filteredCustomers.forEach { customer ->
                    DropdownMenuItem(
                        text = { Text(customer.name) },
                        onClick = {
                            customerQuery = customer.name
                            expandedCustomer = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- FILTRO DE PRODUCTOS Y PRECIO AUTOMÁTICO ---
        ExposedDropdownMenuBox(
            expanded = expandedProduct && filteredProducts.isNotEmpty(),
            onExpandedChange = { expandedProduct = it }
        ) {
            OutlinedTextField(
                value = productQuery,
                onValueChange = {
                    productQuery = it
                    expandedProduct = true
                },
                label = { Text("Buscar Producto...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedProduct && filteredProducts.isNotEmpty(),
                onDismissRequest = { expandedProduct = false }
            ) {
                filteredProducts.forEach { product ->
                    DropdownMenuItem(
                        text = { Text("${product.name} (Stock: ${product.stock} - \$${product.price})") },
                        onClick = {
                            productQuery = product.name
                            selectedProductPrice = product.price
                            expandedProduct = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (selectedProductPrice > 0) "\$${selectedProductPrice * (quantityText.toIntOrNull() ?: 1)}" else "$0.0",
                onValueChange = {},
                readOnly = true,
                label = { Text("Monto Total") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text("Cantidad") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isCredit,
                onCheckedChange = { isCredit = it }
            )
            Text("Venta al Crédito")
        }

        message?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it, color = if (it.contains("éxit")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val qty = quantityText.toIntOrNull() ?: 0
                val type = if (isCredit) "CREDITO" else "CONTADO"
                if (customerQuery.isNotBlank() && productQuery.isNotBlank() && qty > 0) {
                    onAddSale(customerQuery, productQuery, qty, type) { success, msg ->
                        message = msg
                        if (success) {
                            customerQuery = ""
                            productQuery = ""
                            quantityText = "1"
                            selectedProductPrice = 0.0
                            isCredit = false
                        }
                    }
                } else {
                    message = "Complete todos los campos correctamente."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Venta y Descontar Stock")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Historial de Ventas", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sales) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Cliente: ${sale.customerName}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Detalle: ${sale.productName} | Total: \$${sale.amount}")
                        Text(text = "Fecha: ${sale.date}")
                        Text(
                            text = "Tipo: ${sale.saleType}",
                            color = if (sale.saleType == "CREDITO") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
