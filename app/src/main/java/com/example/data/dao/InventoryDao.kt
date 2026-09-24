package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import com.example.data.model.StockTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE stockQty <= minStockAlert ORDER BY (stockQty - minStockAlert) ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE stockQty <= (minStockAlert / 2) ORDER BY stockQty ASC")
    fun getCriticalStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE stockQty <= minStockAlert")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT SUM(stockQty * purchasePrice) FROM products")
    fun getTotalStockValuation(): Flow<Double?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductByIdFlow(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE grade = :grade ORDER BY name ASC")
    fun getProductsByGrade(grade: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR grade LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET stockQty = stockQty + :quantityChange, lastRestocked = :timestamp WHERE id = :productId")
    suspend fun adjustStock(productId: Long, quantityChange: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stockQty = :newStock, lastRestocked = :timestamp WHERE id = :productId")
    suspend fun setStockQuantity(productId: Long, newStock: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET minStockAlert = :threshold WHERE id = :productId")
    suspend fun updateStockAlertThreshold(productId: Long, threshold: Int)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    // Audit logs for stock transactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(transaction: StockTransaction): Long

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY timestamp DESC")
    fun getTransactionsForProduct(productId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 50): Flow<List<StockTransaction>>
}
