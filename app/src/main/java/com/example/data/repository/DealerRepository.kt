package com.example.data.repository

import com.example.data.dao.DealerDao
import com.example.data.model.Dealer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DealerRepository(
    private val dao: DealerDao
) {
    /**
     * Reactive stream of all registered dealers.
     */
    val allDealers: Flow<List<Dealer>> = dao.getAllDealers()

    /**
     * Reactive stream of dealers currently carrying an outstanding credit balance.
     */
    val dealersWithCredit: Flow<List<Dealer>> = dao.getDealersWithOutstandingCredit()

    /**
     * Total credit balance owed across all dealers in PKR.
     */
    val totalCreditBalance: Flow<Double> = dao.getTotalCreditBalance().map { it ?: 0.0 }

    /**
     * Live count of registered dealers.
     */
    val dealersCount: Flow<Int> = dao.getDealersCount()

    // --- CREATE ---
    suspend fun createDealer(dealer: Dealer): Long {
        val sanitized = sanitizeDealer(dealer)
        return dao.insertDealer(sanitized)
    }

    suspend fun insertDealers(dealers: List<Dealer>) {
        val sanitized = dealers.map { sanitizeDealer(it) }
        dao.insertDealers(sanitized)
    }

    // --- READ ---
    suspend fun getDealerById(id: Long): Dealer? = dao.getDealerById(id)

    fun getDealerByIdFlow(id: Long): Flow<Dealer?> = dao.getDealerByIdFlow(id)

    fun searchDealers(query: String): Flow<List<Dealer>> = dao.searchDealers(query)

    // --- UPDATE ---
    suspend fun updateDealer(dealer: Dealer) {
        val sanitized = sanitizeDealer(dealer)
        dao.updateDealer(sanitized)
    }

    suspend fun updateCreditBalance(dealerId: Long, newBalance: Double) {
        dao.updateCreditBalance(dealerId, newBalance)
    }

    suspend fun adjustCreditBalance(dealerId: Long, balanceChange: Double) {
        dao.adjustCreditBalance(dealerId, balanceChange)
    }

    // --- DELETE ---
    suspend fun deleteDealer(dealer: Dealer) {
        dao.deleteDealer(dealer)
    }

    suspend fun deleteDealerById(id: Long) {
        dao.deleteDealerById(id)
    }

    suspend fun deleteAllDealers() {
        dao.deleteAllDealers()
    }

    private fun sanitizeDealer(dealer: Dealer): Dealer {
        val effectiveName = dealer.name.ifBlank { dealer.shopName }
        val effectiveContact = dealer.contact.ifBlank { dealer.phone }
        val effectiveLocation = dealer.location.ifBlank {
            if (dealer.address.isNotBlank() && dealer.city.isNotBlank()) "${dealer.address}, ${dealer.city}"
            else dealer.address.ifBlank { dealer.city }
        }
        val effectiveBalance = if (dealer.currentCreditBalance != 0.0) {
            dealer.currentCreditBalance
        } else {
            dealer.outstandingBalance
        }

        return dealer.copy(
            name = effectiveName,
            shopName = effectiveName,
            contact = effectiveContact,
            phone = effectiveContact,
            location = effectiveLocation,
            address = dealer.address.ifBlank { effectiveLocation },
            city = dealer.city.ifBlank { "Local Market" },
            currentCreditBalance = effectiveBalance,
            outstandingBalance = effectiveBalance
        )
    }
}
