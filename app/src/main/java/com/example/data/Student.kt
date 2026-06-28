package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fatherName: String,
    val grade: String, // e.g., "Grade 1" to "Grade 12"
    val admissionDate: String, // dd.mm.yyyy
    val parentContact1: String,
    val parentContact2: String = "",
    val fixedFee: Double
)
