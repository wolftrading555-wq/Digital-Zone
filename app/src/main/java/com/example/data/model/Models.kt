package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String = "",
    val name: String,
    val brand: String = "",
    val grade: String,             // e.g. 20W-50, 10W-40, 5W-30, 0W-20, ATF, Gear 85W-140
    val category: String,          // Synthetic, Semi-Synthetic, Mineral, Heavy Diesel, Motorcycle, Transmission, Gear Oil
    val packSize: String,          // 1L, 4L, 5L, 20L Pail, 208L Drum
    val stockQty: Int,
    val minStockAlert: Int = 10,
    val optimalStock: Int = 40,
    val purchasePrice: Double,     // PKR cost per unit
    val wholesalePrice: Double,    // PKR dealer selling price
    val retailPrice: Double,       // PKR end-user MRP
    val location: String = "Main Warehouse",
    val lastRestocked: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean get() = stockQty <= minStockAlert
    val isCritical: Boolean get() = stockQty <= (minStockAlert / 2)
    val isOutOfStock: Boolean get() = stockQty <= 0
    val stockDeficit: Int get() = if (stockQty < minStockAlert) minStockAlert - stockQty else 0
    val suggestedReorderQty: Int get() = (optimalStock - stockQty).coerceAtLeast(minStockAlert)
}

enum class AlertSeverity {
    NORMAL,
    LOW_STOCK,
    CRITICAL,
    OUT_OF_STOCK
}

data class LowStockAlert(
    val product: Product,
    val currentStock: Int,
    val threshold: Int,
    val deficit: Int,
    val suggestedReorder: Int,
    val severity: AlertSeverity,
    val estimatedRestockCost: Double
)

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val grade: String = "",
    val changeQty: Int,           // e.g., +50 (restock) or -4 (sale)
    val previousStock: Int,
    val resultingStock: Int,
    val transactionType: String,  // "PURCHASE_RESTOCK", "SALE_DELIVERY", "STOCK_ADJUSTMENT", "DAMAGE"
    val timestamp: Long = System.currentTimeMillis(),
    val referenceNote: String = ""
)

@Entity(tableName = "dealers")
data class Dealer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",                         // Dealer / Shop name
    val contact: String = "",                      // Contact phone / number
    val location: String = "",                     // Location / Address / Market
    val currentCreditBalance: Double = 0.0,        // Current credit balance (PKR)
    val shopName: String = "",                     // Business / Shop trading name
    val ownerName: String = "",                    // Owner / Contact Person
    val phone: String = "",                        // Phone number
    val city: String = "",                         // City
    val address: String = "",                      // Street Address
    val creditLimit: Double = 300000.0,            // Credit limit in PKR
    val outstandingBalance: Double = 0.0,          // Outstanding balance alias
    val status: String = "Active",                 // Status: Active, Overdue, Critical
    val lastVisitDate: Long = System.currentTimeMillis()
) {
    val displayName: String get() = name.ifBlank { shopName }
    val displayContact: String get() = contact.ifBlank { phone }
    val displayLocation: String get() = location.ifBlank { if (city.isNotBlank()) "$address, $city" else address }
    val effectiveBalance: Double get() = if (currentCreditBalance != 0.0) currentCreditBalance else outstandingBalance
}

@Entity(tableName = "sales_orders")
data class SaleOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNo: String,
    val dealerId: Long,
    val dealerName: String,
    val salesOfficerId: Long,
    val salesOfficerName: String,
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val discount: Double = 0.0,
    val netAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double,
    val status: String = "Delivered", // Delivered, Pending, Booked
    val notes: String = ""
)

@Entity(tableName = "sale_order_items")
data class SaleOrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val grade: String,
    val packSize: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

@Entity(tableName = "recovery_payments")
data class RecoveryPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNo: String,
    val dealerId: Long,
    val dealerName: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMode: String,       // Cash, Cheque, Bank Transfer, Online
    val referenceNo: String = "",
    val collectedBy: String,       // Officer Name or Owner
    val chequeDate: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "sales_officers")
data class SalesOfficer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val territory: String,
    val monthlyTarget: Double,     // in PKR
    val currentSales: Double,      // in PKR
    val currentRecovery: Double,   // in PKR
    val active: Boolean = true
) {
    val achievementPercentage: Int
        get() = if (monthlyTarget > 0) ((currentSales / monthlyTarget) * 100).toInt() else 0
}

@Entity(tableName = "gps_visit_logs")
data class GpsVisitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val salesOfficerName: String,
    val dealerId: Long,
    val dealerName: String,
    val address: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val purpose: String,           // Order Booking, Recovery, Stock Audit, General Visit
    val orderBookedAmount: Double = 0.0,
    val recoveryCollected: Double = 0.0,
    val remarks: String = ""
)
