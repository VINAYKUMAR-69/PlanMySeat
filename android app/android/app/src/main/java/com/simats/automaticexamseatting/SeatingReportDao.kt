package com.simats.automaticexamseatting

import androidx.room.*

@Dao
interface SeatingReportDao {
    @Query("SELECT * FROM seating_reports")
    suspend fun getAllReports(): List<SeatingReportEntity>

    @Query("SELECT * FROM seating_reports WHERE roomNumber = :roomNumber")
    suspend fun getReportsByRoom(roomNumber: String): List<SeatingReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<SeatingReportEntity>)

    @Query("DELETE FROM seating_reports")
    suspend fun deleteAllReports()
}
