package com.example.tracsactiontracker.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracsactiontracker.data.BudgetEntity
import com.example.tracsactiontracker.data.BudgetDao
import com.example.tracsactiontracker.data.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    fun getAllBudgets(): Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }

    companion object {
        fun provideFactory(
            budgetDao: BudgetDao,
            transactionDao: TransactionDao
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BudgetViewModel(budgetDao, transactionDao) as T
            }
        }
    }
} 