package com.example.tracsactiontracker

import android.app.Application
import com.example.tracsactiontracker.data.AppDatabase
import com.example.tracsactiontracker.data.SettingsManager

class TracsactionTrackerApplication : Application() {
    lateinit var database: AppDatabase
        private set
    
    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        settingsManager = SettingsManager(this)
    }
} 