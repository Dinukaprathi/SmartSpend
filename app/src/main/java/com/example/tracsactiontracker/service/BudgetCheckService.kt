package com.example.tracsactiontracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.tracsactiontracker.MainActivity
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.TracsactionTrackerApplication
import com.example.tracsactiontracker.data.BudgetEntity
import com.example.tracsactiontracker.data.BudgetDao
import com.example.tracsactiontracker.data.TransactionDao
import com.example.tracsactiontracker.notification.BudgetNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class BudgetCheckService : Service() {
    private lateinit var budgetDao: BudgetDao
    private lateinit var transactionDao: TransactionDao
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        val database = (application as TracsactionTrackerApplication).database
        budgetDao = database.budgetDao()
        transactionDao = database.transactionDao()
        BudgetNotificationChannel.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job = CoroutineScope(Dispatchers.IO).launch {
            checkBudget()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun checkBudget() {
        val currentDate = Date()
        val currentBudget = budgetDao.getCurrentBudget(System.currentTimeMillis()).first()
        
        if (currentBudget != null) {
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val monthlyExpenses = transactionDao.getTotalByDateRange("EXPENSE", startOfMonth, endOfMonth).first() ?: 0.0
            val percentage = (monthlyExpenses / currentBudget.amount) * 100

            if (percentage >= 80) {
                showBudgetWarningNotification(percentage.toInt())
            }
        }
    }

    private fun showBudgetWarningNotification(percentage: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.budget_warning_title))
            .setContentText(getString(R.string.budget_warning_message, percentage))
            .setSmallIcon(R.drawable.ic_warning)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.budget_warning_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.budget_warning_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "budget_warning_channel"
        private const val NOTIFICATION_ID = 1
    }
} 