package com.example.tracsactiontracker.util

import android.content.Context
import android.content.Intent
import com.example.tracsactiontracker.service.BudgetCheckService

object BudgetServiceManager {
    fun startBudgetCheckService(context: Context) {
        val intent = Intent(context, BudgetCheckService::class.java)
        context.startForegroundService(intent)
    }

    fun stopBudgetCheckService(context: Context) {
        val intent = Intent(context, BudgetCheckService::class.java)
        context.stopService(intent)
    }
} 