package com.simats.automaticexamseatting

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val regNo: String,
    val branch: String,
    val year: Int,
    val examType: String = ""
)
