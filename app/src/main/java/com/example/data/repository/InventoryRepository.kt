package com.example.data.repository

import com.example.data.dao.InventoryDao
import com.example.data.model.AlertSeverity
import com.example.data.model.LowStockAlert
import com.example.data.model.Product
import com.example.data.model.StockTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepository(
    private val dao: InventoryDao
) {
    /**
     * Flow of all engine oil products in warehouse inventory.
     */
    val allProducts: Flow<List<Product>> = dao.getAllProducts()

    /**
     * Flow of products that have reached or dropped below their minimum stock alert threshold.
     */
    val lowStockProducts: Flow<List<Product>> = dao.getLowStockProducts()

    /**
     * Flow of critical low stock items (<= 50% of threshold or completely zero).
     */
    val criticalStockProducts: Flow<List<Product>> = dao.getCriticalStockProducts()

    /**
     * Total valuation of warehouse inventory based on purchase/cost price.
     */
    val totalStockValuation: Flow<Double> = dao.getTotalStockValuation().map { it ?: 0.0 }

    /**
     * Live count of items requiring restock attention.
     */
    val lowStockCount: Flow<Int> = dao.getLowStockCount()

    /**
     * Live stream of detailed low-stock alerts with calculated restock suggestions & severity.
     */
    val lowStockAlerts: Flow<List<LowStockAlert>> = dao.getLowStockProducts().map { list ->
        list.map { product ->
            val severity = when {
                product.stockQty <= 0 -> AlertSeverity.OUT_OF_STOCK
                product.stockQty <= (product.minStockAlert / 2) -> AlertSeverity.CRITICAL
                else -> AlertSeverity.LOW_STOCK
            }
            val deficit = (product.minStockAlert - product.stockQty).coerceAtLeast(0)
            val suggestedReorder = (product.optimalStock - product.stockQty).coerceAtLeast(product.minStockAlert)
            val estimatedCost = suggestedReorder * product.purchasePrice

            LowStockAlert(
                product = product,
                currentStock = product.stockQty,
                threshold = product.minStockAlert,
                deficit = deficit,
                suggestedReorder = suggestedReorder,
                severity = severity,
                estimatedRestockCost = estimatedCost
            )
        }
    }

    /**
     * Recent stock transaction history / audit trail.
     */
    val recentStockTransactions: Flow<List<StockTransaction>> = dao.getRecentTransactions(50)

    suspend fun getProductById(id: Long): Product? = dao.getProductById(id)

    fun getProductByIdFlow(id: Long): Flow<Product?> = dao.getProductByIdFlow(id)

    fun searchProducts(query: String): Flow<List<Product>> = dao.searchProducts(query)

    fun getProductsByGrade(grade: String): Flow<List<Product>> = dao.getProductsByGrade(grade)

    fun getProductsByCategory(category: String): Flow<List<Product>> = dao.getProductsByCategory(category)

    suspend fun addProduct(product: Product): Long {
        val id = dao.insertProduct(product)
        if (product.stockQty > 0) {
            dao.insertStockTransaction(
                StockTransaction(
                    productId = id,
                    productName = product.name,
                    grade = product.grade,
                    changeQty = product.stockQty,
                    previousStock = 0,
                    resultingStock = product.stockQty,
                    transactionType = "INITIAL_STOCK",
                    referenceNote = "Opening stock balance"
                )
            )
        }
        return id
    }

    suspend fun updateProduct(product: Product) {
        dao.updateProduct(product)
    }

    /**
     * Adjusts the stock level of an engine oil product and creates an audit transaction log.
     */
    suspend fun adjustStock(
        productId: Long,
        quantityChange: Int,
        transactionType: String = "STOCK_ADJUSTMENT",
        reason: String = ""
    ) {
        val existing = dao.getProductById(productId) ?: return
        val previous = existing.stockQty
        val resulting = (previous + quantityChange).coerceAtLeast(0)

        dao.setStockQuantity(productId, resulting)
        dao.insertStockTransaction(
            StockTransaction(
                productId = productId,
                productName = existing.name,
                grade = existing.grade,
                changeQty = quantityChange,
                previousStock = previous,
                resultingStock = resulting,
                transactionType = transactionType,
                referenceNote = reason
            )
        )
    }

    /**
     * Convenience method to receive new stock shipment / replenishment.
     */
    suspend fun restockProduct(productId: Long, quantityReceived: Int, invoiceRef: String = "") {
        adjustStock(
            productId = productId,
            quantityChange = quantityReceived,
            transactionType = "PURCHASE_RESTOCK",
            reason = "Restocked: $invoiceRef"
        )
    }

    /**
     * Updates minimum stock threshold for alerts.
     */
    suspend fun updateLowStockAlertThreshold(productId: Long, newThreshold: Int) {
        dao.updateStockAlertThreshold(productId, newThreshold)
    }

    suspend fun deleteProduct(id: Long) {
        dao.deleteProduct(id)
    }

    fun getProductAuditHistory(productId: Long): Flow<List<StockTransaction>> =
        dao.getTransactionsForProduct(productId)
}
