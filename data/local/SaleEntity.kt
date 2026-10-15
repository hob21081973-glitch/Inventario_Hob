package com.tuempresa.inventario.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val productName: String,
    val amount: Double,
    val saleType: String, // "CONTADO" o "CREDITO"
    val date: String,
    val isPaid: Boolean = false // Para control de pagos en crédito
)
