package com.example.tracsactiontracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracsactiontracker.data.TransactionDao
import com.example.tracsactiontracker.data.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionViewModel(private val transactionDao: TransactionDao) : ViewModel() {
    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.updateTransaction(transaction)
        }
    }

    companion object {
        fun provideFactory(transactionDao: TransactionDao): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TransactionViewModel(transactionDao) as T
                }
            }
        }
    }
} 