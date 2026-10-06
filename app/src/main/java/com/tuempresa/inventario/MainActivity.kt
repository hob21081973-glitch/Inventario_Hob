package com.tuempresa.inventario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tuempresa.inventario.data.local.InventoryDatabase
import com.tuempresa.inventario.data.repository.ProductRepository
import com.tuempresa.inventario.ui.product.ProductScreen
import com.tuempresa.inventario.ui.product.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos los componentes de datos de forma limpia
        val database = InventoryDatabase.getDatabase(applicationContext)
        val repository = ProductRepository(database.productDao())
        val viewModel = ProductViewModel(repository)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProductScreen(viewModel = viewModel)
                }
            }
        }
    }
}
