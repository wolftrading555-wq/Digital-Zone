package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.data.model.Product
import com.example.data.model.RecoveryPayment
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import com.example.data.model.SalesOfficer
import kotlinx.coroutines.flow.Flow

@Dao
interface OilDao {

    // --- Products / Inventory ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE stockQty <= minStockAlert")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET stockQty = stockQty + :quantityChange WHERE id = :productId")
    suspend fun adjustStock(productId: Long, quantityChange: Int)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    // --- Dealers ---
    @Query("SELECT * FROM dealers ORDER BY shopName ASC")
    fun getAllDealers(): Flow<List<Dealer>>

    @Query("SELECT * FROM dealers WHERE id = :id")
    fun getDealerByIdFlow(id: Long): Flow<Dealer?>

    @Query("SELECT * FROM dealers WHERE id = :id")
    suspend fun getDealerById(id: Long): Dealer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDealer(dealer: Dealer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDealers(dealers: List<Dealer>)

    @Update
    suspend fun updateDealer(dealer: Dealer)

    @Query("UPDATE dealers SET outstandingBalance = outstandingBalance + :balanceChange WHERE id = :dealerId")
    suspend fun updateDealerBalance(dealerId: Long, balanceChange: Double)

    @Query("DELETE FROM dealers WHERE id = :id")
    suspend fun deleteDealer(id: Long)

    // --- Sales Orders ---
    @Query("SELECT * FROM sales_orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<SaleOrder>>

    @Query("SELECT * FROM sales_orders WHERE dealerId = :dealerId ORDER BY date DESC")
    fun getOrdersByDealer(dealerId: Long): Flow<List<SaleOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SaleOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<SaleOrderItem>)

    @Query("SELECT * FROM sale_order_items WHERE orderId = :orderId")
    fun getItemsForOrder(orderId: Long): Flow<List<SaleOrderItem>>

    // --- Recovery Payments ---
    @Query("SELECT * FROM recovery_payments ORDER BY date DESC")
    fun getAllRecoveries(): Flow<List<RecoveryPayment>>

    @Query("SELECT * FROM recovery_payments WHERE dealerId = :dealerId ORDER BY date DESC")
    fun getRecoveriesByDealer(dealerId: Long): Flow<List<RecoveryPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecovery(recovery: RecoveryPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoveries(recoveries: List<RecoveryPayment>)

    // --- Sales Officers ---
    @Query("SELECT * FROM sales_officers ORDER BY currentSales DESC")
    fun getAllSalesOfficers(): Flow<List<SalesOfficer>>

    @Query("SELECT * FROM sales_officers WHERE id = :id")
    suspend fun getSalesOfficerById(id: Long): SalesOfficer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOfficer(officer: SalesOfficer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOfficers(officers: List<SalesOfficer>)

    @Update
    suspend fun updateSalesOfficer(officer: SalesOfficer)

    @Query("UPDATE sales_officers SET currentSales = currentSales + :saleAmount WHERE id = :officerId")
    suspend fun incrementOfficerSales(officerId: Long, saleAmount: Double)

    @Query("UPDATE sales_officers SET currentRecovery = currentRecovery + :recoveryAmount WHERE id = :officerId")
    suspend fun incrementOfficerRecovery(officerId: Long, recoveryAmount: Double)

    // --- GPS Field Visits ---
    @Query("SELECT * FROM gps_visit_logs ORDER BY timestamp DESC")
    fun getAllVisits(): Flow<List<GpsVisitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: GpsVisitLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisits(visits: List<GpsVisitLog>)
}
