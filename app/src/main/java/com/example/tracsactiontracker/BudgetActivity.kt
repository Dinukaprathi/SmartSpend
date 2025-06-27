package com.example.tracsactiontracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

class BudgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        findViewById<ImageButton>(R.id.budget_back_button)?.setOnClickListener {
            finish()
        }
    }
}
