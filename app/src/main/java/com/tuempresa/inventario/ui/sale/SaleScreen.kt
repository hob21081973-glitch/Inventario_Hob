package com.tuempresa.inventario.ui.sale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuempresa.inventario.data.local.SaleEntity

@Composable
fun SaleScreen(
    sales: List<SaleEntity>,
    onAddSale: (String, String, Double, String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Nueva Venta", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Cliente") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Producto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto ($)") },
            modifier = Modifier.fillMaxWidth()
        )
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

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val total = amount.toDoubleOrNull() ?: 0.0
                if (customerName.isNotBlank() && productName.isNotBlank() && total > 0) {
                    val type = if (isCredit) "CREDITO" else "CONTADO"
                    onAddSale(customerName, productName, total, type)
                    customerName = ""
                    productName = ""
                    amount = ""
                    isCredit = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Venta")
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
                        Text(text = "Producto: ${sale.productName} | Total: \$${sale.amount}")
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
