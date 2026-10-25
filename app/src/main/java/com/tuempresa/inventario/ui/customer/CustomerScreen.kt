package com.tuempresa.inventario.ui.customer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tuempresa.inventario.data.local.CustomerEntity
import com.tuempresa.inventario.ui.product.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(viewModel: ProductViewModel) {
    val context = LocalContext.current
    val rawCustomers by viewModel.allCustomers.collectAsState()

    // 🔤 Ordenar clientes alfabéticamente de la A a la Z
    val customers = remember(rawCustomers) {
        rawCustomers.sortedBy { it.name.lowercase() }
    }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    
    // Estado para controlar la visibilidad del diálogo de búsqueda con lupa
    var showSearchDialog by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Gestión de Clientes", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // Fila para el campo de texto de nuevo cliente y la lupa exclusiva de búsqueda
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Cliente (Nuevo)") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester) // 🎯 Control de foco
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Botón con Lupa exclusivo para buscar clientes existentes para editar/eliminar
            IconButton(
                onClick = { showSearchDialog = true },
                modifier = Modifier.size(48dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar cliente existente",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.insertCustomer(name, phone, address) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        name = ""
                        phone = ""
                        address = ""
                        // Regresar el foco al campo Nombre del Cliente
                        try {
                            focusRequester.requestFocus()
                        } catch (_: Exception) {}
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Cliente")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        Text("Clientes Registrados (A-Z)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(customers) { customer ->
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
                            Text(text = customer.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Tel: ${customer.phone}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Dir: ${customer.address}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { customerToEdit = customer }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteCustomer(customer) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Búsqueda con Lupa para Editar o Eliminar clientes específicos
    if (showSearchDialog) {
        SearchCustomerDialog(
            customers = customers,
            onDismiss = { showSearchDialog = false },
            onEdit = { customer ->
                showSearchDialog = false
                customerToEdit = customer
            },
            onDelete = { customer ->
                showSearchDialog = false
                viewModel.deleteCustomer(customer)
                Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    customerToEdit?.let { cust ->
        EditCustomerDialog(
            customer = cust,
            onDismiss = { customerToEdit = null },
            onSave = { updatedName, updatedPhone, updatedAddress ->
                viewModel.updateCustomer(cust.copy(name = updatedName, phone = updatedPhone, address = updatedAddress))
                Toast.makeText(context, "Cliente actualizado con éxito", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SearchCustomerDialog(
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onEdit: (CustomerEntity) -> Unit,
    onDelete: (CustomerEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCustomers = remember(searchQuery, customers) {
        if (searchQuery.isBlank()) customers
        else customers.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buscar Cliente (Editar / Eliminar)") },
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
                    label = { Text("Escriba para filtrar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCustomers) { cust ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = cust.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Tel: ${cust.phone}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { onEdit(cust) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { onDelete(cust) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun EditCustomerDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var address by remember { mutableStateOf(customer.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onSave(name.trim(), phone.trim(), address.trim())
                    onDismiss()
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
