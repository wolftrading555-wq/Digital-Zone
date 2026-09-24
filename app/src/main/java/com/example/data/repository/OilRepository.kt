package com.example.data.repository

import com.example.data.dao.OilDao
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.data.model.Product
import com.example.data.model.RecoveryPayment
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import com.example.data.model.SalesOfficer
import kotlinx.coroutines.flow.Flow

class OilRepository(private val dao: OilDao) {

    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = dao.getLowStockProducts()
    val allDealers: Flow<List<Dealer>> = dao.getAllDealers()
    val allOrders: Flow<List<SaleOrder>> = dao.getAllOrders()
    val allRecoveries: Flow<List<RecoveryPayment>> = dao.getAllRecoveries()
    val allSalesOfficers: Flow<List<SalesOfficer>> = dao.getAllSalesOfficers()
    val allVisits: Flow<List<GpsVisitLog>> = dao.getAllVisits()

    fun getDealerOrders(dealerId: Long): Flow<List<SaleOrder>> = dao.getOrdersByDealer(dealerId)
    fun getDealerRecoveries(dealerId: Long): Flow<List<RecoveryPayment>> = dao.getRecoveriesByDealer(dealerId)
    fun getOrderItems(orderId: Long): Flow<List<SaleOrderItem>> = dao.getItemsForOrder(orderId)

    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun adjustStock(productId: Long, delta: Int) = dao.adjustStock(productId, delta)
    suspend fun deleteProduct(id: Long) = dao.deleteProduct(id)

    suspend fun insertDealer(dealer: Dealer): Long = dao.insertDealer(dealer)
    suspend fun updateDealer(dealer: Dealer) = dao.updateDealer(dealer)
    suspend fun deleteDealer(id: Long) = dao.deleteDealer(id)

    suspend fun recordSale(
        order: SaleOrder,
        items: List<SaleOrderItem>,
        reduceInventory: Boolean = true
    ): Long {
        val orderId = dao.insertOrder(order)
        val itemsWithId = items.map { it.copy(orderId = orderId) }
        dao.insertOrderItems(itemsWithId)

        // Increase dealer outstanding balance by unpaid amount
        if (order.balanceAmount > 0) {
            dao.updateDealerBalance(order.dealerId, order.balanceAmount)
        }

        // Reduce inventory
        if (reduceInventory) {
            for (item in items) {
                dao.adjustStock(item.productId, -item.quantity)
            }
        }

        // Update sales officer stats
        if (order.salesOfficerId > 0) {
            dao.incrementOfficerSales(order.salesOfficerId, order.netAmount)
            if (order.paidAmount > 0) {
                dao.incrementOfficerRecovery(order.salesOfficerId, order.paidAmount)
            }
        }

        return orderId
    }

    suspend fun recordRecovery(recovery: RecoveryPayment): Long {
        val recId = dao.insertRecovery(recovery)
        // Deduct from dealer's outstanding balance
        dao.updateDealerBalance(recovery.dealerId, -recovery.amount)
        return recId
    }

    suspend fun insertVisit(visit: GpsVisitLog): Long = dao.insertVisit(visit)

    suspend fun insertSalesOfficer(officer: SalesOfficer): Long = dao.insertSalesOfficer(officer)
    suspend fun updateSalesOfficer(officer: SalesOfficer) = dao.updateSalesOfficer(officer)
}
