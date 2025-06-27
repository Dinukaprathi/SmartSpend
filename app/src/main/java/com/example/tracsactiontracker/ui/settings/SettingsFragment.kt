package com.example.tracsactiontracker.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.TracsactionTrackerApplication
import com.example.tracsactiontracker.adapter.CategoryAdapter
import com.example.tracsactiontracker.data.Category
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SettingsFragment : Fragment() {
    private lateinit var spinnerCurrency: Spinner
    private lateinit var sliderWarningThreshold: Slider
    private lateinit var tvWarningThresholdValue: MaterialTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private val gson = Gson()
    private val prefs by lazy { requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE) }

    companion object {
        private const val KEY_CURRENCY = "currency"
        private const val DEFAULT_CURRENCY = "USD"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        spinnerCurrency = view.findViewById(R.id.spinnerCurrency)
        sliderWarningThreshold = view.findViewById(R.id.sliderWarningThreshold)
        tvWarningThresholdValue = view.findViewById(R.id.tvWarningThresholdValue)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Setup currency spinner
        setupCurrencySpinner()
        setupBudgetWarningSlider()

        // Setup categories recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CategoryAdapter { categoryName ->
            Toast.makeText(requireContext(), "Category: $categoryName", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // Load saved categories
        val categories = getSavedCategories()
        adapter.updateCategories(categories)
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "INR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter

        // Load saved currency
        val savedCurrency = prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY)
        val position = currencies.indexOf(savedCurrency)
        if (position != -1) {
            spinnerCurrency.setSelection(position)
        }

        // Save currency when changed
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCurrency = currencies[position]
                prefs.edit().putString(KEY_CURRENCY, selectedCurrency).apply()
                // Update application settings
                (activity?.application as? TracsactionTrackerApplication)?.settingsManager?.currency = selectedCurrency
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupBudgetWarningSlider() {
        val settingsManager = (activity?.application as? TracsactionTrackerApplication)?.settingsManager
        if (settingsManager != null) {
            sliderWarningThreshold.value = settingsManager.budgetWarningThreshold.toFloat()

            sliderWarningThreshold.addOnChangeListener { _, value, _ ->
                settingsManager.budgetWarningThreshold = value.toDouble()
            }
        }
    }

    private fun getSavedCategories(): List<Category> {
        val categoriesJson = prefs.getString("categories", "[]")
        return try {
            val type = object : TypeToken<List<Category>>() {}.type
            gson.fromJson(categoriesJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onPause() {
        super.onPause()
        // Save categories
        val categoriesJson = gson.toJson(adapter.getCategories())
        prefs.edit().putString("categories", categoriesJson).apply()
    }
} 