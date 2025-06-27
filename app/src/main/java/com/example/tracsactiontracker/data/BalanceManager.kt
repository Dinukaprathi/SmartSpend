package com.example.tracsactiontracker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BalanceManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("balance_prefs", Context.MODE_PRIVATE)
    private val _totalBalance = MutableLiveData<Double>()
    private val _monthlyBudget = MutableLiveData<Double>()
    private val _totalIncome = MutableLiveData<Double>()
    private val _totalExpenses = MutableLiveData<Double>()
    private val _errorState = MutableLiveData<String?>()

    val totalBalance: LiveData<Double> = _totalBalance
    val monthlyBudget: LiveData<Double> = _monthlyBudget
    val totalIncome: LiveData<Double> = _totalIncome
    val totalExpenses: LiveData<Double> = _totalExpenses
    val errorState: LiveData<String?> = _errorState

    init {
        loadBalances()
    }

    private fun loadBalances() {
        _totalBalance.value = prefs.getFloat("total_balance", 0f).toDouble()
        _monthlyBudget.value = prefs.getFloat("monthly_budget", 0f).toDouble()
        _totalIncome.value = prefs.getFloat("total_income", 0f).toDouble()
        _totalExpenses.value = prefs.getFloat("total_expenses", 0f).toDouble()
    }

    suspend fun updateBalance(amount: Double, isIncome: Boolean) {
        withContext(Dispatchers.Main) {
            val currentBalance = _totalBalance.value ?: 0.0
            val newBalance = if (isIncome) {
                currentBalance + amount
            } else {
                currentBalance - amount
            }
            _totalBalance.value = newBalance
            prefs.edit().putFloat("total_balance", newBalance.toFloat()).apply()

            // Update income/expenses
            if (isIncome) {
                val newIncome = (_totalIncome.value ?: 0.0) + amount
                _totalIncome.value = newIncome
                prefs.edit().putFloat("total_income", newIncome.toFloat()).apply()
            } else {
                val newExpenses = (_totalExpenses.value ?: 0.0) + amount
                _totalExpenses.value = newExpenses
                prefs.edit().putFloat("total_expenses", newExpenses.toFloat()).apply()
            }
        }
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
    }

    fun resetMonthlyBalances() {
        _totalIncome.value = 0.0
        _totalExpenses.value = 0.0
        prefs.edit()
            .putFloat("total_income", 0f)
            .putFloat("total_expenses", 0f)
            .apply()
    }

    private fun handleError(error: Throwable) {
        _errorState.value = error.message ?: "An error occurred"
    }
} 