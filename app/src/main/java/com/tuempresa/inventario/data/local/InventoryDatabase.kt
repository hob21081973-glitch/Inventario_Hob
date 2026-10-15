package com.tuempresa.inventario.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tuempresa.inventario.data.local.ProductEntity
import com.tuempresa.inventario.data.local.CustomerEntity
import com.tuempresa.inventario.data.local.SaleEntity

@Database(
    entities = [ProductEntity::class, CustomerEntity::class, SaleEntity::class],
    version = 2,
    exportSchema = false
)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
}
