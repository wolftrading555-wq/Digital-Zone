package com.example.data.repository

import com.example.data.dao.GpsVisitDao
import com.example.data.model.GpsVisitLog
import com.example.data.service.GpsLocationService
import kotlinx.coroutines.flow.Flow

class GpsVisitRepository(
    private val dao: GpsVisitDao,
    private val locationService: GpsLocationService? = null
) {
    /**
     * Live stream of all GPS field visit records across all dealers.
     */
    val allVisits: Flow<List<GpsVisitLog>> = dao.getAllVisits()

    /**
     * History of visits for a specific dealer, ordered by timestamp descending.
     */
    fun getVisitsByDealer(dealerId: Long): Flow<List<GpsVisitLog>> =
        dao.getVisitsByDealer(dealerId)

    /**
     * Total visit count recorded for a specific dealer.
     */
    fun getVisitsCountForDealer(dealerId: Long): Flow<Int> =
        dao.getVisitsCountForDealer(dealerId)

    /**
     * Captures and stores a GPS visit log with specified coordinates.
     */
    suspend fun recordVisit(
        salesOfficerName: String,
        dealerId: Long,
        dealerName: String,
        address: String,
        latitude: Double,
        longitude: Double,
        purpose: String,
        orderBookedAmount: Double = 0.0,
        recoveryCollected: Double = 0.0,
        remarks: String = ""
    ): Long {
        val visit = GpsVisitLog(
            salesOfficerName = salesOfficerName,
            dealerId = dealerId,
            dealerName = dealerName,
            address = address,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            purpose = purpose,
            orderBookedAmount = orderBookedAmount,
            recoveryCollected = recoveryCollected,
            remarks = remarks
        )
        return dao.insertVisit(visit)
    }

    /**
     * Captures live GPS coordinates using GpsLocationService (or falls back if unavailable)
     * and persists the sales visit to the Room database.
     */
    suspend fun captureAndRecordVisit(
        salesOfficerName: String,
        dealerId: Long,
        dealerName: String,
        address: String,
        fallbackLatitude: Double = 31.5204,
        fallbackLongitude: Double = 74.3587,
        purpose: String,
        orderBookedAmount: Double = 0.0,
        recoveryCollected: Double = 0.0,
        remarks: String = ""
    ): Long {
        val liveCoords = locationService?.getCurrentLocation()
        val lat = liveCoords?.latitude ?: fallbackLatitude
        val lng = liveCoords?.longitude ?: fallbackLongitude

        return recordVisit(
            salesOfficerName = salesOfficerName,
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
    }

    suspend fun insertVisits(visits: List<GpsVisitLog>) {
        dao.insertVisits(visits)
    }

    suspend fun deleteVisit(id: Long) {
        dao.deleteVisitById(id)
    }
}
