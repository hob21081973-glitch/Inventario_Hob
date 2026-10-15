package com.tuempresa.inventario.repository

import com.tuempresa.inventario.data.local.*
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val saleDao: SaleDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    suspend fun insertProduct(product: ProductEntity) = productDao.insertProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    suspend fun insertCustomer(customer: CustomerEntity) = customerDao.insertCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    suspend fun insertSale(sale: SaleEntity) = saleDao.insertSale(sale)
    suspend fun deleteSale(sale: SaleEntity) = saleDao.deleteSale(sale)
}
