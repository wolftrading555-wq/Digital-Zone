package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.data.model.LowStockAlert
import com.example.data.model.Product
import com.example.data.model.RecoveryPayment
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import com.example.data.model.SalesOfficer
import com.example.data.model.StockTransaction
import com.example.data.repository.DealerRepository
import com.example.data.repository.GpsVisitRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.OilRepository
import com.example.data.service.GpsLocationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyChartPoint(
    val month: String,
    val sales: Double,
    val recovery: Double
)

data class DashboardStats(
    val todaySales: Double = 185000.0,
    val todayRecovery: Double = 72500.0,
    val totalOutstanding: Double = 1250000.0,
    val totalStockValue: Double = 3450000.0,
    val totalProductsCount: Int = 9,
    val lowStockCount: Int = 0,
    val activeDealersCount: Int = 5,
    val monthlyData: List<MonthlyChartPoint> = emptyList()
)

sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object Inventory : AppScreen()
    data object Dealers : AppScreen()
    data class DealerLedger(val dealerId: Long) : AppScreen()
    data object CreditRecovery : AppScreen()
    data object SalesTeam : AppScreen()
    data object GpsVisits : AppScreen()
    data object Reports : AppScreen()
}

class OilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OilRepository
    val inventoryRepository: InventoryRepository
    val dealerRepository: DealerRepository
    val gpsLocationService: GpsLocationService
    val gpsVisitRepository: GpsVisitRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        gpsLocationService = GpsLocationService(application)
        repository = OilRepository(database.oilDao())
        inventoryRepository = InventoryRepository(database.inventoryDao())
        dealerRepository = DealerRepository(database.dealerDao())
        gpsVisitRepository = GpsVisitRepository(database.gpsVisitDao(), gpsLocationService)
    }

    val products: StateFlow<List<Product>> = inventoryRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = inventoryRepository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockAlerts: StateFlow<List<LowStockAlert>> = inventoryRepository.lowStockAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val criticalStockProducts: StateFlow<List<Product>> = inventoryRepository.criticalStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dealers: StateFlow<List<Dealer>> = dealerRepository.allDealers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<SaleOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recoveries: StateFlow<List<RecoveryPayment>> = repository.allRecoveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesOfficers: StateFlow<List<SalesOfficer>> = repository.allSalesOfficers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visits: StateFlow<List<GpsVisitLog>> = gpsVisitRepository.allVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _userRole = MutableStateFlow("Owner")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // Monthly baseline chart data (Apr to Sep)
    private val baselineMonthly = listOf(
        MonthlyChartPoint("Apr", 950000.0, 820000.0),
        MonthlyChartPoint("May", 1100000.0, 940000.0),
        MonthlyChartPoint("Jun", 1350000.0, 1150000.0),
        MonthlyChartPoint("Jul", 1200000.0, 1050000.0),
        MonthlyChartPoint("Aug", 1480000.0, 1300000.0),
        MonthlyChartPoint("Sep", 1620000.0, 1420000.0)
    )

    val dashboardStats: StateFlow<DashboardStats> = combine(
        products, dealers, orders, recoveries
    ) { prodList, dealerList, orderList, recList ->
        val stockVal = if (prodList.isNotEmpty()) {
            prodList.sumOf { it.stockQty * it.purchasePrice }
        } else {
            3450000.0
        }

        val outstanding = if (dealerList.isNotEmpty()) {
            dealerList.sumOf { it.outstandingBalance }
        } else {
            1250000.0
        }

        val startOfDay = getStartOfDayMillis()
        val todayOrderSum = orderList.filter { it.date >= startOfDay }.sumOf { it.netAmount }
        val finalTodaySales = if (todayOrderSum > 0) todayOrderSum else 185000.0

        val todayRecSum = recList.filter { it.date >= startOfDay }.sumOf { it.amount }
        val finalTodayRecovery = if (todayRecSum > 0) todayRecSum else 72500.0

        val lowCount = prodList.count { it.stockQty <= it.minStockAlert }

        DashboardStats(
            todaySales = finalTodaySales,
            todayRecovery = finalTodayRecovery,
            totalOutstanding = outstanding,
            totalStockValue = stockVal,
            totalProductsCount = prodList.size,
            lowStockCount = lowCount,
            activeDealersCount = dealerList.size,
            monthlyData = baselineMonthly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setUserRole(role: String) {
        _userRole.value = role
    }

    // Helper to format currency in Pakistani Rupees (PKR)
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale.US)
        format.maximumFractionDigits = 0
        return "Rs. ${format.format(amount)}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun getStartOfDayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Create a new Sale Order
    fun createSaleOrder(
        dealerId: Long,
        dealerName: String,
        officerId: Long,
        officerName: String,
        items: List<SaleOrderItem>,
        discount: Double,
        paidAmount: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val total = items.sumOf { it.lineTotal }
            val net = total - discount
            val balance = (net - paidAmount).coerceAtLeast(0.0)
            val invNumber = "INV-2026-${(1000..9999).random()}"

            val order = SaleOrder(
                invoiceNo = invNumber,
                dealerId = dealerId,
                dealerName = dealerName,
                salesOfficerId = officerId,
                salesOfficerName = officerName,
                date = System.currentTimeMillis(),
                totalAmount = total,
                discount = discount,
                netAmount = net,
                paidAmount = paidAmount,
                balanceAmount = balance,
                notes = notes
            )
            repository.recordSale(order, items)
            onSuccess()
        }
    }

    // Record Recovery / Payment Collection
    fun recordRecovery(
        dealerId: Long,
        dealerName: String,
        amount: Double,
        paymentMode: String,
        referenceNo: String,
        collectedBy: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val receiptNumber = "REC-${(1000..9999).random()}"
            val recovery = RecoveryPayment(
                receiptNo = receiptNumber,
                dealerId = dealerId,
                dealerName = dealerName,
                amount = amount,
                paymentMode = paymentMode,
                referenceNo = referenceNo,
                collectedBy = collectedBy,
                notes = notes
            )
            repository.recordRecovery(recovery)
            onSuccess()
        }
    }

    // Inventory operations
    fun addOrUpdateProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (product.id == 0L) {
                inventoryRepository.addProduct(product)
            } else {
                inventoryRepository.updateProduct(product)
            }
            onSuccess()
        }
    }

    fun adjustStock(productId: Long, delta: Int, reason: String = "Manual Adjustment") {
        viewModelScope.launch {
            inventoryRepository.adjustStock(productId, delta, if (delta > 0) "STOCK_RESTOCK" else "STOCK_DEDUCTION", reason)
        }
    }

    fun restockProduct(productId: Long, quantity: Int, invoiceRef: String = "") {
        viewModelScope.launch {
            inventoryRepository.restockProduct(productId, quantity, invoiceRef)
        }
    }

    fun updateStockAlertThreshold(productId: Long, threshold: Int) {
        viewModelScope.launch {
            inventoryRepository.updateLowStockAlertThreshold(productId, threshold)
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            inventoryRepository.deleteProduct(id)
        }
    }

    // Dealer operations (CRUD)
    fun addOrUpdateDealer(dealer: Dealer, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (dealer.id == 0L) {
                dealerRepository.createDealer(dealer)
            } else {
                dealerRepository.updateDealer(dealer)
            }
            onSuccess()
        }
    }

    fun deleteDealer(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            dealerRepository.deleteDealerById(id)
            onSuccess()
        }
    }

    fun updateDealerCreditBalance(dealerId: Long, newBalance: Double) {
        viewModelScope.launch {
            dealerRepository.updateCreditBalance(dealerId, newBalance)
        }
    }

    // GPS Visit check-in and capture
    fun logGpsVisit(
        officerName: String,
        dealerId: Long,
        dealerName: String,
        address: String,
        lat: Double,
        lng: Double,
        purpose: String,
        orderBookedAmount: Double,
        recoveryCollected: Double,
        remarks: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            gpsVisitRepository.recordVisit(
                salesOfficerName = officerName,
                dealerId = dealerId,
                dealerName = dealerName,
                address = address,
                latitude = lat,
                longitude = lng,
                purpose = purpose,
                orderBookedAmount = orderBookedAmount,
                recoveryCollected = recoveryCollected,
                remarks = remarks
            )
            onSuccess()
        }
    }

    fun captureLiveGpsAndLogVisit(
        officerName: String,
        dealerId: Long,
        dealerName: String,
        address: String,
        fallbackLat: Double,
        fallbackLng: Double,
        purpose: String,
        orderBookedAmount: Double,
        recoveryCollected: Double,
        remarks: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            gpsVisitRepository.captureAndRecordVisit(
                salesOfficerName = officerName,
                dealerId = dealerId,
                dealerName = dealerName,
                address = address,
                fallbackLatitude = fallbackLat,
                fallbackLongitude = fallbackLng,
                purpose = purpose,
                orderBookedAmount = orderBookedAmount,
                recoveryCollected = recoveryCollected,
                remarks = remarks
            )
            onSuccess()
        }
    }

    // Per-Dealer Visit History flows
    fun getDealerVisits(dealerId: Long): Flow<List<GpsVisitLog>> =
        gpsVisitRepository.getVisitsByDealer(dealerId)

    fun getDealerVisitsCount(dealerId: Long): Flow<Int> =
        gpsVisitRepository.getVisitsCountForDealer(dealerId)

    // Sales Officer Target update
    fun updateOfficerTarget(officer: SalesOfficer, newTarget: Double) {
        viewModelScope.launch {
            repository.updateSalesOfficer(officer.copy(monthlyTarget = newTarget))
        }
    }

    fun addSalesOfficer(officer: SalesOfficer, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertSalesOfficer(officer)
            onSuccess()
        }
    }

    fun getDealerOrders(dealerId: Long) = repository.getDealerOrders(dealerId)
    fun getDealerRecoveries(dealerId: Long) = repository.getDealerRecoveries(dealerId)
}
