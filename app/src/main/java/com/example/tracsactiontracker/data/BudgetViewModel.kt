package com.example.tracsactiontracker.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {
    val allBudgets: Flow<List<BudgetEntity>> = repository.allBudgets

    fun insertBudget(budget: BudgetEntity) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun getBudgetsByCategory(category: String): Flow<List<BudgetEntity>> {
        return repository.getBudgetsByCategory(category)
    }

    fun deleteBudget(budget: BudgetEntity) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }
}

class BudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 