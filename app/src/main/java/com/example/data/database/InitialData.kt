package com.example.data.database

import com.example.data.dao.OilDao
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.data.model.Product
import com.example.data.model.RecoveryPayment
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import com.example.data.model.SalesOfficer

object InitialData {
    suspend fun populateDatabase(dao: OilDao) {
        // Sales Officers
        val officers = listOf(
            SalesOfficer(
                id = 1,
                name = "Ali",
                phone = "0300-8451290",
                territory = "Zone 1 - Central Auto Market",
                monthlyTarget = 500000.0,
                currentSales = 425000.0,
                currentRecovery = 310000.0
            ),
            SalesOfficer(
                id = 2,
                name = "Ahmed",
                phone = "0321-4198234",
                territory = "Zone 2 - North Highway & Badami Bagh",
                monthlyTarget = 400000.0,
                currentSales = 288000.0,
                currentRecovery = 220000.0
            ),
            SalesOfficer(
                id = 3,
                name = "Usman",
                phone = "0333-5120938",
                territory = "Zone 3 - Industrial Area & Fleets",
                monthlyTarget = 600000.0,
                currentSales = 564000.0,
                currentRecovery = 490000.0
            )
        )
        dao.insertSalesOfficers(officers)

        // Products (Stock calculated to total ~Rs. 3,450,000 at purchase price / wholesale)
        val products = listOf(
            Product(
                id = 1,
                name = "Shell Helix HX7 10W-40 Synthetic",
                grade = "10W-40",
                category = "Semi-Synthetic",
                packSize = "4L",
                stockQty = 120,
                minStockAlert = 20,
                purchasePrice = 5200.0,
                wholesalePrice = 5850.0,
                retailPrice = 6400.0
            ), // 120 * 5200 = 624,000
            Product(
                id = 2,
                name = "Castrol GTX 20W-50 Engine Oil",
                grade = "20W-50",
                category = "Mineral",
                packSize = "4L",
                stockQty = 140,
                minStockAlert = 25,
                purchasePrice = 4100.0,
                wholesalePrice = 4650.0,
                retailPrice = 5100.0
            ), // 140 * 4100 = 574,000
            Product(
                id = 3,
                name = "Mobil 1 Advanced 5W-30 Full Synthetic",
                grade = "5W-30",
                category = "Synthetic",
                packSize = "4L",
                stockQty = 55,
                minStockAlert = 15,
                purchasePrice = 9500.0,
                wholesalePrice = 10600.0,
                retailPrice = 11800.0
            ), // 55 * 9500 = 522,500
            Product(
                id = 4,
                name = "Total Rubia TIR 7400 15W-40 (Heavy Diesel)",
                grade = "15W-40",
                category = "Heavy Diesel",
                packSize = "20L Pail",
                stockQty = 35,
                minStockAlert = 10,
                purchasePrice = 16800.0,
                wholesalePrice = 18500.0,
                retailPrice = 20200.0
            ), // 35 * 16800 = 588,000
            Product(
                id = 5,
                name = "ZIC X7 5W-30 Fully Synthetic",
                grade = "5W-30",
                category = "Synthetic",
                packSize = "4L",
                stockQty = 80,
                minStockAlert = 20,
                purchasePrice = 5400.0,
                wholesalePrice = 6100.0,
                retailPrice = 6700.0
            ), // 80 * 5400 = 432,000
            Product(
                id = 6,
                name = "Caltex Havoline Formula 20W-50",
                grade = "20W-50",
                category = "Mineral",
                packSize = "4L",
                stockQty = 90,
                minStockAlert = 25,
                purchasePrice = 3800.0,
                wholesalePrice = 4300.0,
                retailPrice = 4750.0
            ), // 90 * 3800 = 342,000
            Product(
                id = 7,
                name = "Atlas Honda 4T Premium Motorcycle Oil",
                grade = "20W-40",
                category = "Motorcycle",
                packSize = "0.7L",
                stockQty = 260,
                minStockAlert = 50,
                purchasePrice = 850.0,
                wholesalePrice = 960.0,
                retailPrice = 1080.0
            ), // 260 * 850 = 221,000
            Product(
                id = 8,
                name = "Apex ATF Dexron III Transmission Fluid",
                grade = "ATF D-III",
                category = "Transmission",
                packSize = "1L",
                stockQty = 95,
                minStockAlert = 20,
                purchasePrice = 1100.0,
                wholesalePrice = 1350.0,
                retailPrice = 1550.0
            ), // 95 * 1100 = 104,500
            Product(
                id = 9,
                name = "Industrial Gear Oil EP 85W-140",
                grade = "85W-140",
                category = "Gear Oil",
                packSize = "4L",
                stockQty = 15,
                minStockAlert = 20,
                purchasePrice = 2800.0,
                wholesalePrice = 3300.0,
                retailPrice = 3700.0
            )  // 15 * 2800 = 42,000 -> Total = 624k+574k+522.5k+588k+432k+342k+221k+104.5k+42k = 3,450,000 exactly!
        )
        dao.insertProducts(products)

        // Dealers (Outstanding balances sum to Rs. 1,250,000 exactly)
        val dealers = listOf(
            Dealer(
                id = 1,
                name = "Bismillah Auto & Oil Traders",
                shopName = "Bismillah Auto & Oil Traders",
                ownerName = "Haji Munir",
                contact = "0300-9421102",
                phone = "0300-9421102",
                city = "Lahore",
                location = "Shop 14, Montgomery Road, Lahore",
                address = "Shop 14, Montgomery Road",
                creditLimit = 400000.0,
                currentCreditBalance = 320000.0,
                outstandingBalance = 320000.0,
                status = "Active"
            ),
            Dealer(
                id = 2,
                name = "Madina Lubricants & Filter House",
                shopName = "Madina Lubricants & Filter House",
                ownerName = "Tariq Mahmood",
                contact = "0322-8812734",
                phone = "0322-8812734",
                city = "Lahore",
                location = "Circular Road, Badami Bagh, Lahore",
                address = "Circular Road, Badami Bagh",
                creditLimit = 350000.0,
                currentCreditBalance = 245000.0,
                outstandingBalance = 245000.0,
                status = "Active"
            ),
            Dealer(
                id = 3,
                name = "Al-Rehman Motors & Oil Service",
                shopName = "Al-Rehman Motors & Oil Service",
                ownerName = "Muhammad Rashid",
                contact = "0334-7123908",
                phone = "0334-7123908",
                city = "Gujranwala",
                location = "GT Road near General Bus Stand, Gujranwala",
                address = "GT Road near General Bus Stand",
                creditLimit = 300000.0,
                currentCreditBalance = 210000.0,
                outstandingBalance = 210000.0,
                status = "Overdue"
            ),
            Dealer(
                id = 4,
                name = "Khyber Diesel & Fleet Spare Parts",
                shopName = "Khyber Diesel & Fleet Spare Parts",
                ownerName = "Gul Khan",
                contact = "0345-9018442",
                phone = "0345-9018442",
                city = "Rawalpindi",
                location = "I-9 Industrial Area, Rawalpindi",
                address = "I-9 Industrial Area",
                creditLimit = 500000.0,
                currentCreditBalance = 295000.0,
                outstandingBalance = 295000.0,
                status = "Active"
            ),
            Dealer(
                id = 5,
                name = "Pak Auto Oil & Grease Center",
                shopName = "Pak Auto Oil & Grease Center",
                ownerName = "Naveed Akhtar",
                contact = "0301-4458921",
                phone = "0301-4458921",
                city = "Faisalabad",
                location = "Tariq Road Auto Market, Faisalabad",
                address = "Tariq Road Auto Market",
                creditLimit = 250000.0,
                currentCreditBalance = 180000.0,
                outstandingBalance = 180000.0,
                status = "Active"
            ) // 320k + 245k + 210k + 295k + 180k = 1,250,000 exactly!
        )
        dao.insertDealers(dealers)

        // Today's Sales Orders (Sum to Rs. 185,000)
        val today = System.currentTimeMillis()
        val orders = listOf(
            SaleOrder(
                id = 1,
                invoiceNo = "INV-2026-0891",
                dealerId = 1,
                dealerName = "Bismillah Auto & Oil Traders",
                salesOfficerId = 1,
                salesOfficerName = "Ali",
                date = today - 3600000 * 2, // 2 hours ago today
                totalAmount = 95000.0,
                discount = 2000.0,
                netAmount = 93000.0,
                paidAmount = 20000.0,
                balanceAmount = 73000.0,
                status = "Delivered",
                notes = "20 cartons Castrol GTX + Shell HX7"
            ),
            SaleOrder(
                id = 2,
                invoiceNo = "INV-2026-0892",
                dealerId = 4,
                dealerName = "Khyber Diesel & Fleet Spare Parts",
                salesOfficerId = 3,
                salesOfficerName = "Usman",
                date = today - 3600000 * 4, // 4 hours ago today
                totalAmount = 92000.0,
                discount = 0.0,
                netAmount = 92000.0,
                paidAmount = 0.0,
                balanceAmount = 92000.0,
                status = "Delivered",
                notes = "5 Pails Total Rubia 20L + 10x 4L"
            ) // 93,000 + 92,000 = 185,000 today's sales!
        )
        for (order in orders) {
            dao.insertOrder(order)
        }

        // Today's Recovery Payments (Sum to Rs. 72,500)
        val recoveries = listOf(
            RecoveryPayment(
                id = 1,
                receiptNo = "REC-4101",
                dealerId = 2,
                dealerName = "Madina Lubricants & Filter House",
                amount = 45000.0,
                date = today - 3600000 * 3,
                paymentMode = "Cheque",
                referenceNo = "CHQ-89214 Meezan Bank",
                collectedBy = "Ahmed",
                notes = "Clearing overdue balance against INV-0810"
            ),
            RecoveryPayment(
                id = 2,
                receiptNo = "REC-4102",
                dealerId = 5,
                dealerName = "Pak Auto Oil & Grease Center",
                amount = 27500.0,
                date = today - 3600000 * 5,
                paymentMode = "Cash",
                referenceNo = "CASH-REC-05",
                collectedBy = "Ali",
                notes = "Weekly routine cash recovery"
            ) // 45,000 + 27,500 = 72,500 today's recovery!
        )
        dao.insertRecoveries(recoveries)

        // GPS Field Visits
        val visits = listOf(
            GpsVisitLog(
                id = 1,
                salesOfficerName = "Ali",
                dealerId = 1,
                dealerName = "Bismillah Auto & Oil Traders",
                address = "Montgomery Road Auto Market, Lahore",
                timestamp = today - 3600000 * 3,
                latitude = 31.5546,
                longitude = 74.3214,
                purpose = "Order Booking",
                orderBookedAmount = 93000.0,
                recoveryCollected = 20000.0,
                remarks = "Dealer ordered fresh stock for motorcycle season"
            ),
            GpsVisitLog(
                id = 2,
                salesOfficerName = "Ahmed",
                dealerId = 2,
                dealerName = "Madina Lubricants & Filter House",
                address = "Circular Road, Badami Bagh, Lahore",
                timestamp = today - 3600000 * 4,
                latitude = 31.5912,
                longitude = 74.3168,
                purpose = "Payment Recovery",
                orderBookedAmount = 0.0,
                recoveryCollected = 45000.0,
                remarks = "Collected Meezan Bank cheque. Next visit on Thursday"
            ),
            GpsVisitLog(
                id = 3,
                salesOfficerName = "Usman",
                dealerId = 4,
                dealerName = "Khyber Diesel & Fleet Spare Parts",
                address = "I-9 Industrial Area, Rawalpindi/Islamabad",
                timestamp = today - 3600000 * 5,
                latitude = 33.6628,
                longitude = 73.0479,
                purpose = "Order Booking",
                orderBookedAmount = 92000.0,
                recoveryCollected = 0.0,
                remarks = "Delivered Total Rubia TIR 7400 to truck workshop"
            )
        )
        dao.insertVisits(visits)
    }
}
