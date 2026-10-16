package com.tuempresa.inventario.ui.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.tuempresa.inventario.data.local.*
import com.tuempresa.inventario.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        InventoryDatabase::class.java,
        "inventory_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = InventoryRepository(
        database.productDao(),
        database.customerDao(),
        database.saleDao()
    )

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertOrUpdateProduct(name: String, price: Double, stock: Int, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val existing = allProducts.value.find { it.name.equals(name.trim(), ignoreCase = true) }
            if (existing != null) {
                val updatedStock = existing.stock + stock
                val updatedProduct = existing.copy(price = price, stock = updatedStock)
                repository.insertProduct(updatedProduct)
                onResult("El producto ya existía. Se sumaron $stock unidades al stock (Total: $updatedStock)")
            } else {
                repository.insertProduct(ProductEntity(name = name.trim(), price = price, stock = stock))
                onResult("Producto agregado exitosamente")
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun insertCustomer(name: String, phone: String, address: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = allCustomers.value.find { it.name.equals(name.trim(), ignoreCase = true) }
            if (existing != null) {
                onResult(false, "Ya existe un cliente registrado con este nombre.")
            } else {
                repository.insertCustomer(CustomerEntity(name = name.trim(), phone = phone, address = address))
                onResult(true, "Cliente registrado con éxito")
            }
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun insertSale(customerName: String, productName: String, quantity: Int, saleType: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val product = allProducts.value.find { it.name.equals(productName, ignoreCase = true) }
            if (product == null) {
                onResult(false, "El producto no existe.")
                return@launch
            }

            if (product.stock < quantity) {
                onResult(false, "Stock insuficiente. Disponibles: ${product.stock}")
                return@launch
            }

            val updatedProduct = product.copy(stock = product.stock - quantity)
            repository.insertProduct(updatedProduct)

            val totalAmount = product.price * quantity
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            repository.insertSale(
                SaleEntity(
                    customerName = customerName,
                    productName = "${product.name} (x$quantity)",
                    amount = totalAmount,
                    saleType = saleType,
                    date = currentDate
                )
            )
            onResult(true, "Venta registrada con éxito")
        }
    }

    fun deleteSaleAndRestoreStock(sale: SaleEntity) {
        viewModelScope.launch {
            try {
                val parts = sale.productName.split(" (x")
                if (parts.size == 2) {
                    val pName = parts[0].trim()
                    val qty = parts[1].replace(")", "").toIntOrNull() ?: 0
                    
                    val product = allProducts.value.find { it.name.equals(pName, ignoreCase = true) }
                    if (product != null) {
                        val restoredStock = product.stock + qty
                        repository.insertProduct(product.copy(stock = restoredStock))
                    }
                }
            } catch (_: Exception) {}

            repository.deleteSale(sale)
        }
    }
}
