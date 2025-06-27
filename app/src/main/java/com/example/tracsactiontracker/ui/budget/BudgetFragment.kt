package com.example.tracsactiontracker.ui.budget

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracsactiontracker.MainActivity
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.TracsactionTrackerApplication
import com.example.tracsactiontracker.adapter.BudgetAdapter
import com.example.tracsactiontracker.data.BudgetDao
import com.example.tracsactiontracker.data.BudgetEntity
import com.example.tracsactiontracker.databinding.FragmentBudgetBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels {
        val database = (requireActivity().application as TracsactionTrackerApplication).database
        BudgetViewModel.provideFactory(database.budgetDao(), database.transactionDao())
    }
    private lateinit var budgetAdapter: BudgetAdapter
    private lateinit var budgetDao: BudgetDao
    private lateinit var etAmount: TextInputEditText
    private lateinit var etCategory: TextInputEditText
    private lateinit var etStartDate: TextInputEditText
    private lateinit var etEndDate: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var currencySpinner: android.widget.Spinner
    private var selectedCurrency: String = "USD"
    private val currencySymbols = mapOf(
        "USD" to "$",
        "LKR" to "Rs",
        "EUR" to "€",
        "AED" to "د.إ"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeBudgets()

        // Initialize database
        val database = (requireActivity().application as TracsactionTrackerApplication).database
        budgetDao = database.budgetDao()

        // Initialize views
        etAmount = binding.etAmount
        etCategory = binding.etCategory
        etStartDate = binding.etStartDate
        etEndDate = binding.etEndDate
        saveButton = binding.saveBudgetButton
        currencySpinner = binding.spinnerCurrencyBudget

        setupCurrencySpinner()
        setupDatePickers()
        setupSaveButton()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter { budget ->
            deleteBudget(budget)
        }
        binding.budgetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
        }
    }

    private fun observeBudgets() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllBudgets().collect { budgets ->
                budgetAdapter.submitList(budgets)
            }
        }
    }

    private fun deleteBudget(budget: BudgetEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteBudget(budget)
        }
    }

    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        etStartDate.setOnClickListener {
            showDatePicker { date ->
                etStartDate.setText(dateFormat.format(date))
            }
        }

        etEndDate.setOnClickListener {
            showDatePicker { date ->
                etEndDate.setText(dateFormat.format(date))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection as Long
            onDateSelected(calendar.time)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setupCurrencySpinner() {
        val currencyList = currencySymbols.values.toList()
        val adapter = android.widget.ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, currencyList)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        currencySpinner.adapter = adapter
        currencySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCurrency = currencySymbols.entries.first { it.value == currencyList[position] }.key
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString()
            val startDate = etStartDate.text.toString()
            val endDate = etEndDate.text.toString()

            if (amount == null || amount < 0) {
                Toast.makeText(context, getString(R.string.error_negative_amount), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category.isBlank() || startDate.isBlank() || endDate.isBlank()) {
                Toast.makeText(context, getString(R.string.error_required_field), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDateObj = dateFormat.parse(startDate)
                val endDateObj = dateFormat.parse(endDate)

                if (startDateObj != null && endDateObj != null) {
                    budgetDao.insertBudget(
                        BudgetEntity(
                            amount = amount,
                            category = category,
                            startDate = startDateObj.time,
                            endDate = endDateObj.time,
                            currency = selectedCurrency
                        )
                    )
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                        // Navigate to main page after saving budget
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun clearInputs() {
        etAmount.text?.clear()
        etCategory.text?.clear()
        etStartDate.text?.clear()
        etEndDate.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 