package com.example.tracsactiontracker.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET_WARNING_THRESHOLD = "budget_warning_threshold"
        private const val DEFAULT_CURRENCY = "USD"
        private const val DEFAULT_WARNING_THRESHOLD = 80.0 // 80% of budget
    }

    var currency: String
        get() = prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var budgetWarningThreshold: Double
        get() = prefs.getFloat(KEY_BUDGET_WARNING_THRESHOLD, DEFAULT_WARNING_THRESHOLD.toFloat()).toDouble()
        set(value) = prefs.edit().putFloat(KEY_BUDGET_WARNING_THRESHOLD, value.toFloat()).apply()

    fun clearSettings() {
        prefs.edit().clear().apply()
    }
} 