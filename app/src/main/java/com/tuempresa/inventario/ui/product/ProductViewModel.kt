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

    fun insertProduct(name: String, price: Double, stock: Int) {
        viewModelScope.launch {
            repository.insertProduct(ProductEntity(name = name, price = price, stock = stock))
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun insertCustomer(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.insertCustomer(CustomerEntity(name = name, phone = phone, address = address))
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // 🚀 NUEVA LÓGICA DE VENTAS CON DESCUENTO DE STOCK
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

            // 1. Descontar stock del producto
            val updatedProduct = product.copy(stock = product.stock - quantity)
            repository.insertProduct(updatedProduct) // Room hace replace por ID

            // 2. Registrar la venta
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
}
