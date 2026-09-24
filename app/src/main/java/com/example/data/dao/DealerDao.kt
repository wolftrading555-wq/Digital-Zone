package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Dealer
import kotlinx.coroutines.flow.Flow

@Dao
interface DealerDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDealer(dealer: Dealer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDealers(dealers: List<Dealer>)

    // --- READ ---
    @Query("SELECT * FROM dealers ORDER BY CASE WHEN name != '' THEN name ELSE shopName END ASC")
    fun getAllDealers(): Flow<List<Dealer>>

    @Query("SELECT * FROM dealers WHERE id = :id")
    suspend fun getDealerById(id: Long): Dealer?

    @Query("SELECT * FROM dealers WHERE id = :id")
    fun getDealerByIdFlow(id: Long): Flow<Dealer?>

    @Query("""
        SELECT * FROM dealers 
        WHERE name LIKE '%' || :query || '%' 
           OR shopName LIKE '%' || :query || '%' 
           OR contact LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
           OR location LIKE '%' || :query || '%' 
           OR city LIKE '%' || :query || '%' 
           OR address LIKE '%' || :query || '%' 
           OR ownerName LIKE '%' || :query || '%'
        ORDER BY CASE WHEN name != '' THEN name ELSE shopName END ASC
    """)
    fun searchDealers(query: String): Flow<List<Dealer>>

    @Query("""
        SELECT * FROM dealers 
        WHERE currentCreditBalance > 0 OR outstandingBalance > 0 
        ORDER BY CASE WHEN currentCreditBalance > 0 THEN currentCreditBalance ELSE outstandingBalance END DESC
    """)
    fun getDealersWithOutstandingCredit(): Flow<List<Dealer>>

    @Query("""
        SELECT SUM(CASE WHEN currentCreditBalance > 0 THEN currentCreditBalance ELSE outstandingBalance END) 
        FROM dealers
    """)
    fun getTotalCreditBalance(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM dealers")
    fun getDealersCount(): Flow<Int>

    // --- UPDATE ---
    @Update
    suspend fun updateDealer(dealer: Dealer)

    @Query("""
        UPDATE dealers 
        SET currentCreditBalance = :newBalance, 
            outstandingBalance = :newBalance 
        WHERE id = :dealerId
    """)
    suspend fun updateCreditBalance(dealerId: Long, newBalance: Double)

    @Query("""
        UPDATE dealers 
        SET currentCreditBalance = currentCreditBalance + :balanceChange, 
            outstandingBalance = outstandingBalance + :balanceChange 
        WHERE id = :dealerId
    """)
    suspend fun adjustCreditBalance(dealerId: Long, balanceChange: Double)

    // --- DELETE ---
    @Delete
    suspend fun deleteDealer(dealer: Dealer)

    @Query("DELETE FROM dealers WHERE id = :id")
    suspend fun deleteDealerById(id: Long)

    @Query("DELETE FROM dealers")
    suspend fun deleteAllDealers()
}
