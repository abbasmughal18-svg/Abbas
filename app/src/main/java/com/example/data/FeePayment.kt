package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val monthYear: String, // e.g., "June 2026", "July 2026"
    val amountPaid: Double,
    val paymentDate: String, // dd.mm.yyyy
    val remarks: String = ""
)
