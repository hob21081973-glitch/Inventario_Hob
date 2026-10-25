package com.tuempresa.inventario.ui.product

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
import com.tuempresa.inventario.data.local.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val context = LocalContext.current
    val rawProducts by viewModel.allProducts.collectAsState()
    
    // 🔤 Ordenar alfabéticamente de la A a la Z
    val products = remember(rawProducts) {
        rawProducts.sortedBy { it.name.lowercase() }
    }

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    
    // Estado para controlar la visibilidad del diálogo de búsqueda con lupa
    var showSearchDialog by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Stock de Productos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // Fila para separar el campo de texto libre y la lupa de búsqueda exclusiva
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Producto (Nuevo)") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester) // 🎯 Control de foco
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Botón con Lupa exclusivo para buscar productos existentes para editar/eliminar
            IconButton(
                onClick = { showSearchDialog = true },
                modifier = Modifier.size(48dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar producto existente",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Precio (L)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = stock,
            onValueChange = { stock = it },
            label = { Text("Unidades en Stock") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val p = price.toDoubleOrNull()
                val s = stock.toIntOrNull()
                if (name.isBlank() || p == null || s == null) {
                    Toast.makeText(context, "Complete los campos correctamente", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.insertOrUpdateProduct(name, p, s) { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    name = ""
                    price = ""
                    stock = ""
                    // Regresar el foco al campo Nombre
                    try {
                        focusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Producto")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        Text("Productos Registrados (A-Z)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
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
                            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Stock: ${product.stock} unidades", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Precio: L ${product.price}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { productToEdit = product }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Búsqueda con Lupa para Editar o Eliminar productos específicos
    if (showSearchDialog) {
        SearchProductDialog(
            products = products,
            onDismiss = { showSearchDialog = false },
            onEdit = { product ->
                showSearchDialog = false
                productToEdit = product
            },
            onDelete = { product ->
                showSearchDialog = false
                viewModel.deleteProduct(product)
                Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    productToEdit?.let { prod ->
        EditProductDialog(
            product = prod,
            onDismiss = { productToEdit = null },
            onSave = { updatedName, updatedPrice, updatedStock ->
                viewModel.updateProduct(prod.copy(name = updatedName, price = updatedPrice, stock = updatedStock))
                Toast.makeText(context, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SearchProductDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buscar Producto (Editar / Eliminar)") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp), // Ajustable según diseño
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
                    items(filteredProducts) { prod ->
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
                                    Text(text = prod.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Stock: ${prod.stock} | L ${prod.price}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { onEdit(prod) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { onDelete(prod) }) {
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
fun EditProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (L)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: product.price
                val s = stock.toIntOrNull() ?: product.stock
                if (name.isNotBlank()) {
                    onSave(name.trim(), p, s)
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
