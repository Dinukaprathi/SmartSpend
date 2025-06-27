package com.example.tracsactiontracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracsactiontracker.data.TransactionEntity
import com.example.tracsactiontracker.data.TransactionRepository
import com.example.tracsactiontracker.data.CategorySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    private val _monthlyExpenses = MutableStateFlow(0.0)
    val monthlyExpenses: StateFlow<Double> = _monthlyExpenses.asStateFlow()

    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()

    private val _categorySummary = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categorySummary: StateFlow<List<CategorySummary>> = _categorySummary.asStateFlow()

    init {
        loadTransactions()
        loadMonthlyData()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _transactions.value = transactions
            }
        }
    }

    private fun loadMonthlyData() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        viewModelScope.launch {
            repository.getMonthlyExpenses(month, year).collect { expenses ->
                _monthlyExpenses.value = expenses
            }
        }

        viewModelScope.launch {
            repository.getMonthlyIncome(month, year).collect { income ->
                _monthlyIncome.value = income
            }
        }

        // Get start and end of month timestamps
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        viewModelScope.launch {
            repository.getCategorySummary("EXPENSE", startOfMonth, endOfMonth).collect { summary ->
                _categorySummary.value = summary
            }
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun getTransactionsByCategory(category: String) {
        viewModelScope.launch {
            repository.getTransactionsByCategory(category).collect { transactions ->
                _transactions.value = transactions
            }
        }
    }

    companion object {
        fun provideFactory(repository: TransactionRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TransactionViewModel(repository) as T
                }
            }
        }
    }
} 