package com.example.tracsactiontracker.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.TracsactionTrackerApplication
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

class SummaryFragment : Fragment() {
    private lateinit var categoryAdapter: CategorySummaryAdapter
    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        val database = (requireActivity().application as TracsactionTrackerApplication).database
        transactionDao = database.transactionDao()
        budgetDao = database.budgetDao()

        setupRecyclerView(view)
        observeSummary(view)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCategorySummary)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategorySummaryAdapter(emptyList())
        recyclerView.adapter = categoryAdapter
    }

    private fun observeSummary(view: View) {
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
                view.findViewById<TextView>(R.id.tvTotalIncome).text = String.format(
                    Locale.getDefault(),
                    "$%.2f",
                    income
                )
                view.findViewById<TextView>(R.id.tvTotalExpenses).text = String.format(
                    Locale.getDefault(),
                    "$%.2f",
                    expenses
                )
                view.findViewById<TextView>(R.id.tvBalance).text = String.format(
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
        view?.let { observeSummary(it) }
    }
} 