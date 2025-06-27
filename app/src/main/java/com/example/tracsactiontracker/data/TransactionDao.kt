package com.example.tracsactiontracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT category, SUM(amount) as totalAmount, COUNT(*) as count FROM transactions WHERE type = :type GROUP BY category")
    fun getCategorySummary(type: String): Flow<List<CategorySummary>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT category, SUM(amount) as totalAmount, COUNT(*) as count 
        FROM transactions 
        WHERE type = :type AND date BETWEEN :startDate AND :endDate
        GROUP BY category
    """)
    fun getCategorySummaryByDateRange(type: String, startDate: Long, endDate: Long): Flow<List<CategorySummary>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    fun getTotalByDateRange(type: String, startDate: Long, endDate: Long): Flow<Double?>
} 