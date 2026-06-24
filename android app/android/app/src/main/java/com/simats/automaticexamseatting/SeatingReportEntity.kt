package com.simats.automaticexamseatting

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seating_reports")
data class SeatingReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val regNo: String,
    val branch: String,
    val year: Int,
    val seatNo: Int,
    val roomNumber: String,
    val building: String,
    val invigilator: String,
    val subject: String,
    val date: String,
    val time: String
)
