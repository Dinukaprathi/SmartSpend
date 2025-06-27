package com.example.tracsactiontracker.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(category)

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    suspend fun insertTransaction(transaction: TransactionEntity) = 
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) = 
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) = 
        transactionDao.deleteTransaction(transaction)

    fun getMonthlyExpenses(month: Int, year: Int): Flow<Double> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.timeInMillis - 1
        return transactionDao.getTotalByDateRange("EXPENSE", startDate, endDate).map { it ?: 0.0 }
    }

    fun getMonthlyIncome(month: Int, year: Int): Flow<Double> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.timeInMillis - 1
        return transactionDao.getTotalByDateRange("INCOME", startDate, endDate).map { it ?: 0.0 }
    }

    fun getCategorySummary(type: String, startDate: Long, endDate: Long): Flow<List<CategorySummary>> =
        transactionDao.getCategorySummaryByDateRange(type, startDate, endDate)

    fun getTotalByType(type: String, startDate: Date, endDate: Date): Flow<Double> =
        transactionDao.getTotalByDateRange(type, startDate.time, endDate.time).map { it ?: 0.0 }
} 