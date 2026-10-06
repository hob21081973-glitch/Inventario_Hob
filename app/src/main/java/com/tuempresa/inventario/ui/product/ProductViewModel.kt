package com.tuempresa.inventario.ui.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tuempresa.inventario.data.local.InventoryDatabase
import com.tuempresa.inventario.data.local.ProductEntity
import com.tuempresa.inventario.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository

    val allProducts: StateFlow<List<ProductEntity>>

    init {
        val productDao = InventoryDatabase.getDatabase(application).productDao()
        repository = ProductRepository(productDao)
        allProducts = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insert(name: String, price: Double, stock: Int) {
        viewModelScope.launch {
            repository.insert(ProductEntity(name = name, price = price, stock = stock))
        }
    }

    fun delete(product: ProductEntity) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }
}
