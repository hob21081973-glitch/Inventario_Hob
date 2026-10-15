package com.tuempresa.inventario.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

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
