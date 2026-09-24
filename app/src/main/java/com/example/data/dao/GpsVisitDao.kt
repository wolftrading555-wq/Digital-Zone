package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GpsVisitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsVisitDao {

    @Query("SELECT * FROM gps_visit_logs ORDER BY timestamp DESC")
    fun getAllVisits(): Flow<List<GpsVisitLog>>

    @Query("SELECT * FROM gps_visit_logs WHERE dealerId = :dealerId ORDER BY timestamp DESC")
    fun getVisitsByDealer(dealerId: Long): Flow<List<GpsVisitLog>>

    @Query("SELECT * FROM gps_visit_logs WHERE id = :id")
    suspend fun getVisitById(id: Long): GpsVisitLog?

    @Query("SELECT COUNT(*) FROM gps_visit_logs WHERE dealerId = :dealerId")
    fun getVisitsCountForDealer(dealerId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM gps_visit_logs")
    fun getTotalVisitsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: GpsVisitLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisits(visits: List<GpsVisitLog>)

    @Query("DELETE FROM gps_visit_logs WHERE id = :id")
    suspend fun deleteVisitById(id: Long)

    @Query("DELETE FROM gps_visit_logs WHERE dealerId = :dealerId")
    suspend fun deleteVisitsByDealer(dealerId: Long)
}
