package com.example.tracsactiontracker.data

import androidx.room.ColumnInfo

data class CategorySummary(
    val category: String,
    @ColumnInfo(name = "totalAmount")
    val total: Double,
    val count: Int
) 