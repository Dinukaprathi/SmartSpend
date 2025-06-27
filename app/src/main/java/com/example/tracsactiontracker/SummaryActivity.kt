package com.example.tracsactiontracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.adapter.CategorySummaryAdapter
import com.example.tracsactiontracker.data.BudgetDao
import com.example.tracsactiontracker.data.TransactionDao
import com.example.tracsactiontracker.data.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SummaryActivity : AppCompatActivity() {
    private lateinit var categoryAdapter: CategorySummaryAdapter
    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Initialize database
        val database = (application as TracsactionTrackerApplication).database
        transactionDao = database.transactionDao()
        budgetDao = database.budgetDao()

        setupRecyclerView()
        observeSummary()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvCategorySummary)
        recyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategorySummaryAdapter(emptyList())
        recyclerView.adapter = categoryAdapter
    }

    private fun observeSummary() {
        CoroutineScope(Dispatchers.Main).launch {
            combine(
                transactionDao.getAllTransactions(),
                budgetDao.getAllBudgets()
            ) { transactions: List<TransactionEntity>, budgets ->
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                // Filter transactions for current month
                val monthlyTransactions = transactions.filter { transaction ->
                    val transactionDate = Calendar.getInstance().apply { timeInMillis = transaction.date }
                    transactionDate.get(Calendar.MONTH) == currentMonth &&
                    transactionDate.get(Calendar.YEAR) == currentYear
                }

                // Calculate totals
                val income = monthlyTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expenses = monthlyTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val budgetAmount = budgets.lastOrNull()?.amount ?: 0.0
                val balance = budgetAmount + income - expenses

                // Update UI
                findViewById<TextView>(R.id.tvTotalIncome).text = String.format(
                    Locale.getDefault(),
                    "$%.2f",
                    income
                )
                findViewById<TextView>(R.id.tvTotalExpenses).text = String.format(
                    Locale.getDefault(),
                    "$%.2f",
                    expenses
                )
                findViewById<TextView>(R.id.tvBalance).text = String.format(
                    Locale.getDefault(),
                    "$%.2f",
                    balance
                )

                // Update category summary
                val categorySummary = monthlyTransactions
                    .filter { it.type == "EXPENSE" }
                    .groupBy { it.category }
                    .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }

                categoryAdapter.updateCategories(categorySummary)
            }.collectLatest { }
        }
    }

    override fun onResume() {
        super.onResume()
        observeSummary()
    }
} 