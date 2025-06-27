package com.example.tracsactiontracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tracsactiontracker.data.BalanceManager
import com.example.tracsactiontracker.data.Category
import com.example.tracsactiontracker.data.TransactionDao
import com.example.tracsactiontracker.data.TransactionEntity
import com.example.tracsactiontracker.data.TransactionType
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var etCategory: TextInputEditText
    private lateinit var etDate: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var rbIncome: RadioButton
    private lateinit var rbExpense: RadioButton
    private lateinit var bottomNavigationView: BottomNavigationView
    private var editingTransaction: TransactionEntity? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val gson = Gson()
    private lateinit var transactionDao: TransactionDao
    private lateinit var balanceManager: BalanceManager
    private var selectedCurrency: String = "USD"
    private val currencySymbols = mapOf(
        "USD" to "$",
        "LKR" to "Rs",
        "EUR" to "€",
        "AED" to "د.إ"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_transaction_activity)
        
        // Get transaction if editing
        editingTransaction = intent.getSerializableExtra("transaction") as? TransactionEntity

        // Initialize views
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        etCategory = findViewById(R.id.etCategory)
        etDate = findViewById(R.id.etDate)
        rgType = findViewById(R.id.rgType)
        rbIncome = findViewById(R.id.rbIncome)
        rbExpense = findViewById(R.id.rbExpense)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Initialize managers
        val database = (application as TracsactionTrackerApplication).database
        transactionDao = database.transactionDao()
        balanceManager = BalanceManager(this)

        // Try to get selected currency from main spinner if exists
        val spinner = findViewById<android.widget.Spinner?>(R.id.spinner_currency_main)
        if (spinner != null && spinner.selectedItem != null) {
            val currencyList = currencySymbols.values.toList()
            val selectedSymbol = spinner.selectedItem.toString()
            selectedCurrency = currencySymbols.entries.firstOrNull { it.value == selectedSymbol }?.key ?: "USD"
        }

        setupDatePicker()
        setupSaveButton()
        setupNavigation()
        populateFields()

        findViewById<ImageButton>(R.id.add_transaction_back_button)?.setOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                etDate.setText(dateFormat.format(calendar.time))
            }, year, month, day).show()
        }
    }

    private fun setupSaveButton() {
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val description = etDescription.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString()
            val date = etDate.text.toString()
            val isIncome = rbIncome.isChecked

            if (description.isBlank() || amount == null || category.isBlank() || date.isBlank()) {
                Toast.makeText(this, getString(R.string.error_required_field), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount < 0) {
                Toast.makeText(this, getString(R.string.error_negative_amount), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = TransactionEntity(
                description = description,
                amount = amount,
                category = category,
                date = dateFormat.parse(date)?.time ?: System.currentTimeMillis(),
                type = if (isIncome) TransactionType.INCOME.name else TransactionType.EXPENSE.name,
                currency = selectedCurrency
            )

            CoroutineScope(Dispatchers.IO).launch {
                transactionDao.insertTransaction(transaction)
                balanceManager.updateBalance(amount, isIncome)
                runOnUiThread {
                    Toast.makeText(this@AddTransactionActivity, getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    finish()
                }
            }
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_add -> {
                    // Already in AddTransactionActivity, do nothing
                    true
                }
                R.id.navigation_summary -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        // Set the Add Transaction item as selected
        bottomNavigationView.selectedItemId = R.id.navigation_add
    }

    private fun getSavedCategories(): List<Category> {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val categoriesJson = prefs.getString("categories", "[]")
        return try {
            val type = object : TypeToken<List<Category>>() {}.type
            gson.fromJson(categoriesJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun populateFields() {
        editingTransaction?.let { transaction ->
            etAmount.setText(String.format("%.2f", transaction.amount))
            etDescription.setText(transaction.description)
            etDate.setText(dateFormat.format(Date(transaction.date)))
            
            // Set the selected category in spinner
            val categories = getSavedCategories()
            val position = categories.indexOfFirst { it.name == transaction.category }
            if (position != -1) {
                etCategory.setText(transaction.category)
            }
            
            // Set the radio button based on transaction type
            if (transaction.type == "INCOME") {
                rbIncome.isChecked = true
            } else {
                rbExpense.isChecked = true
            }
        } ?: run {
            // Set default date to today for new transactions
            etDate.setText(dateFormat.format(Date()))
        }
    }
}