package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.AlertSeverity
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.data.model.Product
import com.example.data.repository.DealerRepository
import com.example.data.repository.GpsVisitRepository
import com.example.data.repository.InventoryRepository
import com.example.data.service.GpsCoordinates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: InventoryRepository
  private lateinit var dealerRepository: DealerRepository
  private lateinit var gpsVisitRepository: GpsVisitRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = InventoryRepository(database.inventoryDao())
    dealerRepository = DealerRepository(database.dealerDao())
    gpsVisitRepository = GpsVisitRepository(database.gpsVisitDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Engine Oil Pro", appName)
  }

  @Test
  fun `verify monthly chart points integrity`() {
    val points = listOf(
      com.example.ui.viewmodel.MonthlyChartPoint("Apr", 950000.0, 820000.0),
      com.example.ui.viewmodel.MonthlyChartPoint("May", 1100000.0, 940000.0),
      com.example.ui.viewmodel.MonthlyChartPoint("Jun", 1350000.0, 1150000.0),
      com.example.ui.viewmodel.MonthlyChartPoint("Jul", 1200000.0, 1050000.0),
      com.example.ui.viewmodel.MonthlyChartPoint("Aug", 1480000.0, 1300000.0),
      com.example.ui.viewmodel.MonthlyChartPoint("Sep", 1620000.0, 1420000.0)
    )
    assertEquals(6, points.size)
    assertEquals("Sep", points.last().month)
    assertEquals(1620000.0, points.last().sales, 0.01)
  }

  @Test
  fun `room schema creates products and tracks engine oil inventory`() = runBlocking {
    val product = Product(
      sku = "SH-10W40-4L",
      name = "Shell Helix HX7 10W-40 Synthetic",
      brand = "Shell",
      grade = "10W-40",
      category = "Semi-Synthetic",
      packSize = "4L",
      stockQty = 120,
      minStockAlert = 20,
      optimalStock = 150,
      purchasePrice = 5200.0,
      wholesalePrice = 5850.0,
      retailPrice = 6400.0
    )

    val productId = repository.addProduct(product)
    assertTrue(productId > 0)

    val loaded = repository.getProductById(productId)
    assertNotNull(loaded)
    assertEquals("10W-40", loaded?.grade)
    assertEquals("Semi-Synthetic", loaded?.category)
    assertEquals(120, loaded?.stockQty)
    assertFalse(loaded?.isLowStock == true)

    val productsList = repository.allProducts.first()
    assertEquals(1, productsList.size)
  }

  @Test
  fun `room repository calculates current stock levels and inventory valuation`() = runBlocking {
    val p1 = Product(
      name = "Castrol GTX 20W-50",
      brand = "Castrol",
      grade = "20W-50",
      category = "Mineral",
      packSize = "4L",
      stockQty = 50,
      minStockAlert = 15,
      purchasePrice = 4000.0,
      wholesalePrice = 4500.0,
      retailPrice = 5000.0
    )
    val p2 = Product(
      name = "Mobil 1 Advanced 5W-30",
      brand = "Mobil",
      grade = "5W-30",
      category = "Synthetic",
      packSize = "4L",
      stockQty = 30,
      minStockAlert = 10,
      purchasePrice = 9000.0,
      wholesalePrice = 10000.0,
      retailPrice = 11000.0
    )

    repository.addProduct(p1)
    repository.addProduct(p2)

    val totalValuation = repository.totalStockValuation.first()
    // 50 * 4000 + 30 * 9000 = 200,000 + 270,000 = 470,000
    assertEquals(470000.0, totalValuation, 0.01)
  }

  @Test
  fun `room repository detects low stock items and generates alerts`() = runBlocking {
    val normalProduct = Product(
      name = "ZIC X7 5W-30",
      grade = "5W-30",
      category = "Synthetic",
      packSize = "4L",
      stockQty = 80,
      minStockAlert = 20,
      purchasePrice = 5000.0,
      wholesalePrice = 5500.0,
      retailPrice = 6000.0
    )
    val lowStockProduct = Product(
      name = "Industrial Gear Oil EP 85W-140",
      grade = "85W-140",
      category = "Gear Oil",
      packSize = "4L",
      stockQty = 8,
      minStockAlert = 20,
      optimalStock = 40,
      purchasePrice = 2800.0,
      wholesalePrice = 3300.0,
      retailPrice = 3700.0
    )

    repository.addProduct(normalProduct)
    val lowId = repository.addProduct(lowStockProduct)

    // Check low stock count
    val lowCount = repository.lowStockCount.first()
    assertEquals(1, lowCount)

    // Check low stock products list
    val lowProducts = repository.lowStockProducts.first()
    assertEquals(1, lowProducts.size)
    assertEquals("Industrial Gear Oil EP 85W-140", lowProducts.first().name)

    // Check low stock alerts flow
    val alerts = repository.lowStockAlerts.first()
    assertEquals(1, alerts.size)
    val alert = alerts.first()
    assertEquals(lowId, alert.product.id)
    assertEquals(8, alert.currentStock)
    assertEquals(20, alert.threshold)
    assertEquals(12, alert.deficit) // 20 - 8 = 12
    assertEquals(32, alert.suggestedReorder) // 40 - 8 = 32
    assertEquals(AlertSeverity.CRITICAL, alert.severity) // 8 <= (20 / 2 = 10) -> CRITICAL
    assertEquals(32 * 2800.0, alert.estimatedRestockCost, 0.01)
  }

  @Test
  fun `stock adjustment and restock updates levels and resolves alerts`() = runBlocking {
    val lowProduct = Product(
      name = "Total Rubia 15W-40",
      grade = "15W-40",
      category = "Heavy Diesel",
      packSize = "20L Pail",
      stockQty = 5,
      minStockAlert = 15,
      purchasePrice = 16000.0,
      wholesalePrice = 18000.0,
      retailPrice = 20000.0
    )
    val id = repository.addProduct(lowProduct)

    // Initial state: in low stock alert
    assertEquals(1, repository.lowStockCount.first())

    // Restock +20 units
    repository.restockProduct(id, 20, "PO-2026-981")

    // Verify stock updated to 25
    val updated = repository.getProductById(id)
    assertNotNull(updated)
    assertEquals(25, updated?.stockQty)
    assertFalse(updated?.isLowStock == true)

    // Verify low stock alert is now resolved (0 alerts)
    assertEquals(0, repository.lowStockCount.first())
    assertTrue(repository.lowStockAlerts.first().isEmpty())

    // Verify audit transactions exist
    val history = repository.getProductAuditHistory(id).first()
    assertTrue(history.isNotEmpty())
    val restockLog = history.find { it.transactionType == "PURCHASE_RESTOCK" }
    assertNotNull(restockLog)
    assertEquals(20, restockLog?.changeQty)
    assertEquals(25, restockLog?.resultingStock)
  }

  @Test
  fun `dealer entity persists record with name contact location and credit balance`() = runBlocking {
    val dealer = Dealer(
      name = "Bismillah Auto & Oil Traders",
      contact = "0300-9421102",
      location = "Shop 14, Montgomery Road, Lahore",
      currentCreditBalance = 320000.0,
      creditLimit = 400000.0,
      ownerName = "Haji Munir"
    )

    val id = dealerRepository.createDealer(dealer)
    assertTrue(id > 0)

    val retrieved = dealerRepository.getDealerById(id)
    assertNotNull(retrieved)
    assertEquals("Bismillah Auto & Oil Traders", retrieved?.name)
    assertEquals("0300-9421102", retrieved?.contact)
    assertEquals("Shop 14, Montgomery Road, Lahore", retrieved?.location)
    assertEquals(320000.0, retrieved?.currentCreditBalance ?: 0.0, 0.01)
    assertEquals("Haji Munir", retrieved?.ownerName)
  }

  @Test
  fun `dealer crud read and search operations`() = runBlocking {
    val d1 = Dealer(
      name = "Madina Lubricants",
      contact = "0322-8812734",
      location = "Badami Bagh, Lahore",
      currentCreditBalance = 245000.0,
      creditLimit = 350000.0
    )
    val d2 = Dealer(
      name = "Al-Rehman Motors",
      contact = "0334-7123908",
      location = "GT Road, Gujranwala",
      currentCreditBalance = 210000.0,
      creditLimit = 300000.0
    )

    dealerRepository.createDealer(d1)
    dealerRepository.createDealer(d2)

    val all = dealerRepository.allDealers.first()
    assertEquals(2, all.size)

    // Search by location
    val searchGujranwala = dealerRepository.searchDealers("Gujranwala").first()
    assertEquals(1, searchGujranwala.size)
    assertEquals("Al-Rehman Motors", searchGujranwala.first().name)

    // Search by contact
    val searchPhone = dealerRepository.searchDealers("0322").first()
    assertEquals(1, searchPhone.size)
    assertEquals("Madina Lubricants", searchPhone.first().name)

    // Verify total credit balance
    val totalCredit = dealerRepository.totalCreditBalance.first()
    assertEquals(455000.0, totalCredit, 0.01)
  }

  @Test
  fun `dealer crud update credit balance and details`() = runBlocking {
    val dealer = Dealer(
      name = "Khyber Diesel & Fleet Parts",
      contact = "0345-9018442",
      location = "I-9 Industrial Area, Rawalpindi",
      currentCreditBalance = 295000.0,
      creditLimit = 500000.0
    )
    val id = dealerRepository.createDealer(dealer)

    // Update details (e.g. phone and location)
    val toUpdate = dealer.copy(
      id = id,
      contact = "0345-1112233",
      location = "I-10 Industrial Area, Islamabad"
    )
    dealerRepository.updateDealer(toUpdate)

    val updated = dealerRepository.getDealerById(id)
    assertEquals("0345-1112233", updated?.contact)
    assertEquals("I-10 Industrial Area, Islamabad", updated?.location)

    // Adjust credit balance (+15,000)
    dealerRepository.adjustCreditBalance(id, 15000.0)
    val adjusted = dealerRepository.getDealerById(id)
    assertEquals(310000.0, adjusted?.currentCreditBalance ?: 0.0, 0.01)

    // Update credit balance directly (e.g. after settlement to 50,000)
    dealerRepository.updateCreditBalance(id, 50000.0)
    val finalBalance = dealerRepository.getDealerById(id)
    assertEquals(50000.0, finalBalance?.currentCreditBalance ?: 0.0, 0.01)
  }

  @Test
  fun `dealer crud delete operation removes record`() = runBlocking {
    val dealer = Dealer(
      name = "Pak Auto Grease Center",
      contact = "0301-4458921",
      location = "Tariq Road, Faisalabad",
      currentCreditBalance = 180000.0,
      creditLimit = 250000.0
    )
    val id = dealerRepository.createDealer(dealer)
    assertEquals(1, dealerRepository.dealersCount.first())

    // Delete dealer
    dealerRepository.deleteDealerById(id)

    assertEquals(0, dealerRepository.dealersCount.first())
    val deleted = dealerRepository.getDealerById(id)
    assertEquals(null, deleted)
  }

  @Test
  fun `gps coordinates formatting and google maps url generation`() {
    val coords = GpsCoordinates(latitude = 31.5204, longitude = 74.3587, accuracy = 4.5f)
    assertTrue(coords.formattedCoordinates.contains("31.5204° N"))
    assertTrue(coords.formattedCoordinates.contains("74.3587° E"))
    assertEquals("https://maps.google.com/?q=31.5204,74.3587", coords.googleMapsUrl)
  }

  @Test
  fun `capturing and storing GPS location coordinates during sales visits`() = runBlocking {
    val visitId = gpsVisitRepository.recordVisit(
      salesOfficerName = "Ali",
      dealerId = 1L,
      dealerName = "Bismillah Auto Traders",
      address = "Montgomery Road, Lahore",
      latitude = 31.5546,
      longitude = 74.3214,
      purpose = "Order Booking",
      orderBookedAmount = 93000.0,
      recoveryCollected = 20000.0,
      remarks = "Dealer booked 4L cartons with geo-stamp"
    )

    assertTrue(visitId > 0)

    val allVisits = gpsVisitRepository.allVisits.first()
    assertEquals(1, allVisits.size)

    val visit = allVisits.first()
    assertEquals("Ali", visit.salesOfficerName)
    assertEquals(1L, visit.dealerId)
    assertEquals("Bismillah Auto Traders", visit.dealerName)
    assertEquals("Montgomery Road, Lahore", visit.address)
    assertEquals(31.5546, visit.latitude, 0.0001)
    assertEquals(74.3214, visit.longitude, 0.0001)
    assertEquals("Order Booking", visit.purpose)
    assertEquals(93000.0, visit.orderBookedAmount, 0.01)
    assertEquals(20000.0, visit.recoveryCollected, 0.01)
    assertEquals("Dealer booked 4L cartons with geo-stamp", visit.remarks)
  }

  @Test
  fun `view a history of visits per dealer returns accurate chronological records`() = runBlocking {
    val dealer1Id = 10L
    val dealer2Id = 20L

    // Record two visits for Dealer 1
    gpsVisitRepository.recordVisit(
      salesOfficerName = "Ali",
      dealerId = dealer1Id,
      dealerName = "Dealer 1 Auto",
      address = "Market 1",
      latitude = 31.5000,
      longitude = 74.3000,
      purpose = "Order Booking",
      orderBookedAmount = 50000.0,
      recoveryCollected = 0.0,
      remarks = "Visit 1 for Dealer 1"
    )

    // Small delay to ensure timestamp difference
    kotlinx.coroutines.delay(10)

    gpsVisitRepository.recordVisit(
      salesOfficerName = "Ahmed",
      dealerId = dealer1Id,
      dealerName = "Dealer 1 Auto",
      address = "Market 1",
      latitude = 31.5002,
      longitude = 74.3005,
      purpose = "Payment Recovery",
      orderBookedAmount = 0.0,
      recoveryCollected = 40000.0,
      remarks = "Visit 2 for Dealer 1"
    )

    // Record one visit for Dealer 2
    gpsVisitRepository.recordVisit(
      salesOfficerName = "Usman",
      dealerId = dealer2Id,
      dealerName = "Dealer 2 Parts",
      address = "Market 2",
      latitude = 33.6000,
      longitude = 73.0000,
      purpose = "Stock Audit",
      orderBookedAmount = 0.0,
      recoveryCollected = 0.0,
      remarks = "Visit for Dealer 2"
    )

    // Query Dealer 1 visit history
    val dealer1Visits = gpsVisitRepository.getVisitsByDealer(dealer1Id).first()
    assertEquals(2, dealer1Visits.size)
    // Most recent visit should be first (ORDER BY timestamp DESC)
    assertEquals("Payment Recovery", dealer1Visits[0].purpose)
    assertEquals("Visit 2 for Dealer 1", dealer1Visits[0].remarks)
    assertEquals(40000.0, dealer1Visits[0].recoveryCollected, 0.01)

    assertEquals("Order Booking", dealer1Visits[1].purpose)
    assertEquals("Visit 1 for Dealer 1", dealer1Visits[1].remarks)

    // Query Dealer 2 visit history
    val dealer2Visits = gpsVisitRepository.getVisitsByDealer(dealer2Id).first()
    assertEquals(1, dealer2Visits.size)
    assertEquals("Usman", dealer2Visits.first().salesOfficerName)
    assertEquals("Dealer 2 Parts", dealer2Visits.first().dealerName)

    // Verify per-dealer visit counts
    assertEquals(2, gpsVisitRepository.getVisitsCountForDealer(dealer1Id).first())
    assertEquals(1, gpsVisitRepository.getVisitsCountForDealer(dealer2Id).first())
    assertEquals(0, gpsVisitRepository.getVisitsCountForDealer(999L).first())
  }
}
