package com.example.tracsactiontracker.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun insertBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }

    fun getBudgetsByCategory(category: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByCategory(category)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }

    fun getCurrentBudget(date: Long): Flow<BudgetEntity?> {
        return budgetDao.getCurrentBudget(date)
    }
} 