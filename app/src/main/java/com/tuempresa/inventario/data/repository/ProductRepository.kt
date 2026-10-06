package com.tuempresa.inventario.repository

import com.tuempresa.inventario.data.local.ProductDao
import com.tuempresa.inventario.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun insert(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun delete(product: ProductEntity) {
        productDao.deleteProduct(product)
    }
}
