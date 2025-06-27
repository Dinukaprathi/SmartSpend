package com.example.tracsactiontracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracsactiontracker.data.BudgetEntity
import com.example.tracsactiontracker.data.BudgetDao
import com.example.tracsactiontracker.data.TransactionDao
import com.example.tracsactiontracker.data.TransactionEntity
import com.example.tracsactiontracker.data.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.time.LocalDate
import java.time.YearMonth

class BudgetViewModel(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _currentBudget = MutableStateFlow<Double>(0.0)
    val currentBudget: StateFlow<Double> = _currentBudget.asStateFlow()

    private val _totalExpenses = MutableStateFlow<Double>(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _budgetWarning = MutableStateFlow<String?>(null)
    val budgetWarning: StateFlow<String?> = _budgetWarning.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                budgetDao.getCurrentBudget(System.currentTimeMillis()).map { it?.amount ?: 0.0 },
                transactionDao.getTotalByDateRange(
                    "EXPENSE",
                    getStartOfMonth(),
                    getEndOfMonth()
                ).map { it ?: 0.0 }
            ) { budget: Double, expenses: Double ->
                _currentBudget.value = budget
                _totalExpenses.value = expenses
                checkBudgetWarning(budget, expenses)
            }.collect()
        }
    }

    fun getAllBudgets(): Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    fun getCurrentBudget(date: Long): Flow<BudgetEntity?> = budgetDao.getCurrentBudget(date)

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }

    fun setBudget(amount: Double) {
        _currentBudget.value = amount
        calculateRemaining()
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            _transactions.value = _transactions.value + transaction
            calculateSpent()
            calculateRemaining()
        }
    }

    fun updateTransactions(transactions: List<TransactionEntity>) {
        viewModelScope.launch {
            _transactions.value = transactions
            calculateSpent()
            calculateRemaining()
        }
    }

    private fun calculateSpent() {
        val currentMonth = YearMonth.now()
        val spentAmount = _transactions.value
            .filter { it.type == TransactionType.EXPENSE.name }
            .filter { 
                val transactionDate = LocalDate.parse(it.date.toString())
                YearMonth.from(transactionDate) == currentMonth
            }
            .sumOf { it.amount }
        _totalExpenses.value = spentAmount
    }

    private fun calculateRemaining() {
        _currentBudget.value = _currentBudget.value - _totalExpenses.value
        checkBudgetWarning(_currentBudget.value, _totalExpenses.value)
    }

    private fun checkBudgetWarning(budget: Double, expenses: Double) {
        if (budget > 0) {
            val percentage = (expenses / budget) * 100
            _budgetWarning.value = when {
                percentage >= 100 -> "Budget exceeded by ${(percentage - 100).toInt()}%"
                percentage >= 80 -> "Warning: You have spent ${percentage.toInt()}% of your budget!"
                else -> null
            }
        } else {
            _budgetWarning.value = null
        }
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    companion object {
        fun provideFactory(
            budgetDao: BudgetDao,
            transactionDao: TransactionDao
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BudgetViewModel(budgetDao, transactionDao) as T
                }
            }
        }
    }
} 