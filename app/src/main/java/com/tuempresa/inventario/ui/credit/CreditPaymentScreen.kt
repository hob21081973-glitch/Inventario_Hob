package com.tuempresa.inventario.ui.credit

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tuempresa.inventario.data.local.CustomerEntity
import com.tuempresa.inventario.data.local.SaleEntity
import com.tuempresa.inventario.ui.product.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditPaymentScreen(viewModel: ProductViewModel) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val sales by viewModel.allSales.collectAsState()

    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var paymentInput by remember { mutableStateOf("") }
    var showCustomerSearchDialog by remember { mutableStateOf(false) }

    val creditSalesOfCustomer = if (selectedCustomer != null) {
        sales.filter { 
            it.customerName.equals(selectedCustomer!!.name.trim(), ignoreCase = true) && 
            it.saleType.equals("CREDITO", ignoreCase = true) 
        }
    } else {
        emptyList()
    }

    val totalDebt = creditSalesOfCustomer.sumOf { it.amount }
    val paymentAmount = paymentInput.toDoubleOrNull() ?: 0.0
    val newBalance = (totalDebt - paymentAmount).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Créditos", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gestión de Abonos y Créditos", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Seleccione un cliente con deudas:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (customers.isEmpty()) {
            Text("No hay clientes registrados.", color = MaterialTheme.colorScheme.error)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.name ?: "Seleccione un cliente...",
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
                        contentDescription = "Buscar cliente para crédito",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedCustomer != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Cliente: ${selectedCustomer!!.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Deuda Total Actual: L $totalDebt", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedTextField(
                        value = paymentInput,
                        onValueChange = { paymentInput = it },
                        label = { Text("Monto del Abono (L)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Nuevo Saldo Restante: L $newBalance", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (paymentAmount <= 0) {
                                Toast.makeText(context, "Ingrese un monto de abono válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (paymentAmount > totalDebt) {
                                Toast.makeText(context, "El abono no puede superar la deuda total", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.applyCreditPayment(selectedCustomer!!.name, paymentAmount) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    paymentInput = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💵 Aplicar Abono / Cancelar Saldo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Detalle de Ventas al Crédito Pendientes:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(creditSalesOfCustomer) { sale ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Fecha: ${sale.date}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Producto: ${sale.productName}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Saldo pendiente: L ${sale.amount}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showCustomerSearchDialog) {
        SearchCustomerForCreditDialog(
            customers = customers,
            onDismiss = { showCustomerSearchDialog = false },
            onSelect = { customer ->
                selectedCustomer = customer
                paymentInput = ""
                showCustomerSearchDialog = false
            }
        )
    }
}

@Composable
fun SearchCustomerForCreditDialog(
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
                        // Solución estable usando .clickable en lugar del Card experimental onClick
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
