package com.example.tracsactiontracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.adapter.CategoryAnalysisAdapter
import com.example.tracsactiontracker.adapter.CategoryAnalysisItem
import com.example.tracsactiontracker.data.TransactionEntity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class CategoryAnalysisActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAnalysisAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_analysis)

        // Initialize views
        pieChart = findViewById(R.id.pieChart)
        recyclerView = findViewById(R.id.rvCategoryAnalysis)
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAnalysisAdapter()
        recyclerView.adapter = adapter

        // Setup pie chart
        setupPieChart()
        
        // Load and display data
        loadCategoryAnalysis()
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(android.R.color.white)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1400)
        pieChart.legend.isEnabled = false
    }

    private fun loadCategoryAnalysis() {
        val database = (application as TracsactionTrackerApplication).database
        val transactionDao = database.transactionDao()

        lifecycleScope.launch {
            // Get current month's transactions
            val calendar = Calendar.getInstance()
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfMonth = calendar.apply {
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }.timeInMillis

            transactionDao.getTransactionsByDateRange(startOfMonth, endOfMonth).collectLatest { transactions ->
                val expenseTransactions = transactions.filter { it.type == "EXPENSE" }
                val totalExpenses = expenseTransactions.sumOf { it.amount }

                // Group transactions by category
                val categoryGroups = expenseTransactions.groupBy { it.category }
                    .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }

                // Create pie chart entries
                val entries = categoryGroups.map { (category, amount) ->
                    PieEntry(amount.toFloat(), category)
                }

                // Create pie chart data set
                val dataSet = PieDataSet(entries, "Categories")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = android.R.color.white

                // Set pie chart data
                val data = PieData(dataSet)
                pieChart.data = data
                pieChart.invalidate()

                // Create adapter items
                val items = categoryGroups.map { (category, amount) ->
                    CategoryAnalysisItem(
                        category = category,
                        amount = amount,
                        percentage = (amount / totalExpenses) * 100
                    )
                }

                adapter.submitList(items)
            }
        }
    }
} 