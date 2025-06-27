package com.example.tracsactiontracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE category = :category")
    fun getBudgetsByCategory(category: String): Flow<List<BudgetEntity>>

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE :date BETWEEN startDate AND endDate LIMIT 1")
    fun getCurrentBudget(date: Long): Flow<BudgetEntity?>
} 