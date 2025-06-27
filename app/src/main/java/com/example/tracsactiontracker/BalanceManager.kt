package com.example.tracsactiontracker

import android.content.Context
import com.example.tracsactiontracker.data.TransactionRepository
import com.example.tracsactiontracker.data.BudgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class BalanceManager(context: Context) {
    private val transactionRepository: TransactionRepository
    private val budgetRepository: BudgetRepository
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(0.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    init {
        val database = (context.applicationContext as TracsactionTrackerApplication).database
        transactionRepository = TransactionRepository(database.transactionDao())
        budgetRepository = BudgetRepository(database.budgetDao())
        
        observeBalances()
    }

    private fun observeBalances() {
        scope.launch {
            val calendar = Calendar.getInstance()
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

            combine(
                transactionRepository.getTotalByType("INCOME", Date(startOfMonth), Date(endOfMonth)),
                transactionRepository.getTotalByType("EXPENSE", Date(startOfMonth), Date(endOfMonth)),
                budgetRepository.getCurrentBudget(System.currentTimeMillis())
            ) { income: Double, expenses: Double, budget: com.example.tracsactiontracker.data.BudgetEntity? ->
                _totalIncome.value = income
                _totalExpenses.value = expenses
                _totalBalance.value = income - expenses
                _monthlyBudget.value = budget?.amount ?: 0.0
            }.collect { }
        }
    }

    fun refreshBalances() {
        observeBalances()
    }
} 