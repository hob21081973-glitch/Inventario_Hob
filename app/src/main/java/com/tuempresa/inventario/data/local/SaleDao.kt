package com.tuempresa.inventario.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE saleType = 'CREDITO'")
    fun getCreditSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE saleType = 'CONTADO'")
    fun getCashSales(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)
}
