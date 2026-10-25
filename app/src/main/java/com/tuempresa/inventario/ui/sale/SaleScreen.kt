package com.tuempresa.inventario.ui.sale

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onAddSale: (String, String, Int, String, (Boolean, String) -> Unit) -> Unit,
    onDeleteSale: (SaleEntity) -> Unit
) {
    val context = LocalContext.current

    var selectedCustomer by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var saleType by remember { mutableStateOf("CONTADO") }

    var showCustomerSearchDialog by remember { mutableStateOf(false) }
    var showProductSearchDialog by remember { mutableStateOf(false) }

    val sortedSales = remember(sales) {
        sales.sortedBy { it.customerName.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Registro de Ventas", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // Selector de Cliente con Lupa
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (selectedCustomer.isEmpty()) "Seleccionar Cliente..." else selectedCustomer,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showCustomerSearchDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar cliente",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de Producto con Lupa
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (selectedProduct.isEmpty()) "Seleccionar Producto..." else selectedProduct,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showProductSearchDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar producto",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantityText,
            onValueChange = { quantityText = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = saleType == "CONTADO",
                    onClick = { saleType = "CONTADO" }
                )
                Text("Contado")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = saleType == "CREDITO",
                    onClick = { saleType = "CREDITO" }
                )
                Text("Crédito")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val qty = quantityText.toIntOrNull()
                if (selectedCustomer.isBlank() || selectedProduct.isBlank() || qty == null || qty <= 0) {
                    Toast.makeText(context, "Complete los datos de la venta correctamente", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                onAddSale(selectedCustomer, selectedProduct, qty, saleType) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        quantityText = ""
                        selectedProduct = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Venta")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        Text("Historial de Ventas (Ordenadas por Cliente A-Z)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedSales) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Cliente: ${sale.customerName}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "Prod: ${sale.productName}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Total: L ${sale.amount} [${sale.saleType}]", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Fecha: ${sale.date}", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { onDeleteSale(sale) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Venta", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showCustomerSearchDialog) {
        SearchCustomerForSaleDialog(
            customers = customers,
            onDismiss = { showCustomerSearchDialog = false },
            onSelect = { customer ->
                selectedCustomer = customer.name
                showCustomerSearchDialog = false
            }
        )
    }

    if (showProductSearchDialog) {
        SearchProductForSaleDialog(
            products = products,
            onDismiss = { showProductSearchDialog = false },
            onSelect = { product ->
                selectedProduct = product.name
                showProductSearchDialog = false
            }
        )
    }
}

@Composable
fun SearchCustomerForSaleDialog(
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onSelect: (CustomerEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, customers) {
        if (searchQuery.isBlank()) customers
        else customers.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Cliente") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Filtrar cliente...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { customer ->
                        // Solución estable usando .clickable en lugar de onClick experimental en Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(customer) },
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = customer.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SearchProductForSaleDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onSelect: (ProductEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Producto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Filtrar producto...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { product ->
                        // Solución estable usando .clickable en lugar de onClick experimental en Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(product) },
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Stock: ${product.stock} - L ${product.price}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
