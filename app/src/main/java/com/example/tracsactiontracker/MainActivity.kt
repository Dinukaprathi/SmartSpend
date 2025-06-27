package com.example.tracsactiontracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.data.TransactionRepository
import com.example.tracsactiontracker.ui.TransactionAdapter
import com.example.tracsactiontracker.ui.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var balanceManager: BalanceManager
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var recentTransactionsAdapter: TransactionAdapter

    private lateinit var currencySpinner: Spinner
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvTotalBalance: TextView

    private val currencySymbols = mapOf(
        "USD" to "$",
        "LKR" to "Rs",
        "EUR" to "€",
        "AED" to "د.إ"
    )

    private var selectedCurrency = "USD"
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        balanceManager = BalanceManager(this)

        val database = (application as TracsactionTrackerApplication).database
        val transactionRepository = TransactionRepository(database.transactionDao())
        transactionViewModel = ViewModelProvider(
            this,
            TransactionViewModel.provideFactory(transactionRepository)
        )[TransactionViewModel::class.java]

        currencySpinner = findViewById(R.id.spinner_currency_main)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)

        setupCurrencySpinner()
        setupBottomNavigation()
        setupRecyclerView()
        setupDatePickers()
        observeBalances()
        observeTransactions()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupCurrencySpinner() {
        val currencyList = currencySymbols.values.toList()
        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, currencyList)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        currencySpinner.adapter = adapter

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCurrency = currencySymbols.entries.first { it.value == currencyList[position] }.key
                updateBalanceViews()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val mainContentContainer = findViewById<View>(R.id.main_content_container)
        val budgetFormContainer = findViewById<View>(R.id.budget_form_container)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_add -> {
                    startActivity(Intent(this@MainActivity, AddTransactionActivity::class.java))
                    true
                }
                R.id.navigation_budget -> {
                    startActivity(Intent(this@MainActivity, BudgetActivity::class.java))
                    true
                }
                R.id.navigation_summary -> {
                    startActivity(Intent(this@MainActivity, SummaryActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    true
                }
                else -> {
                    mainContentContainer.visibility = View.VISIBLE
                    budgetFormContainer.visibility = View.GONE
                    true
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recentTransactionsRecyclerView)
        recentTransactionsAdapter = TransactionAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recentTransactionsAdapter
        }
    }

    private fun setupDatePickers() {
        val startDateInput = findViewById<TextInputEditText>(R.id.startDateInput)
        val endDateInput = findViewById<TextInputEditText>(R.id.endDateInput)

        startDateInput.setOnClickListener { showMaterialDatePicker(startDateInput) }
        endDateInput.setOnClickListener { showMaterialDatePicker(endDateInput) }
    }

    private fun showMaterialDatePicker(inputField: TextInputEditText) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            inputField.setText(dateFormat.format(calendar.time))
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private var lastBalance: Double = 0.0
    private var lastIncome: Double = 0.0
    private var lastExpenses: Double = 0.0

    private fun updateBalanceViews(balance: Double? = null, income: Double? = null, expenses: Double? = null) {
        if (balance != null) lastBalance = balance
        if (income != null) lastIncome = income
        if (expenses != null) lastExpenses = expenses
        tvTotalBalance.text = formatWithCurrency(lastBalance)
        tvTotalIncome.text = formatWithCurrency(lastIncome)
        tvTotalExpenses.text = formatWithCurrency(lastExpenses)
    }

    private fun observeBalances() {
        lifecycleScope.launch {
            balanceManager.totalBalance.collect { balance ->
                updateBalanceViews(balance = balance)
            }
        }
        lifecycleScope.launch {
            balanceManager.totalIncome.collect { income ->
                updateBalanceViews(income = income)
            }
        }
        lifecycleScope.launch {
            balanceManager.totalExpenses.collect { expenses ->
                updateBalanceViews(expenses = expenses)
            }
        }
        lifecycleScope.launch {
            balanceManager.monthlyBudget.collect { budget ->
                findViewById<TextView>(R.id.tvMonthlyBudget).text = formatWithCurrency(budget)
                updateBudgetProgressBar(budget)
            }
        }
    }

    private fun observeTransactions() {
        lifecycleScope.launch {
            transactionViewModel.transactions.collect { transactions ->
                recentTransactionsAdapter.submitList(transactions.take(5))
            }
        }
    }

    private fun formatWithCurrency(amount: Double): String {
        val symbol = currencySymbols[selectedCurrency] ?: "$"
        return "$symbol${"%.2f".format(amount)}"
    }

    private fun updateBudgetProgressBar(budget: Double) {
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.budgetProgressBar)
        val expenses = balanceManager.totalExpenses.value

        if (budget > 0) {
            val progress = ((expenses / budget) * 100).toInt()
            progressBar.progress = progress.coerceIn(0, 100)
        } else {
            progressBar.progress = 0
        }
    }

    override fun onResume() {
        super.onResume()
        balanceManager.refreshBalances()
    }
}