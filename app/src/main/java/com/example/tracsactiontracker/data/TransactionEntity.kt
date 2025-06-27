package com.example.tracsactiontracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val category: String,
    val date: Long, // Stored as timestamp
    val type: String, // "INCOME" or "EXPENSE"
    val currency: String = "USD"
) 