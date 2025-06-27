package com.example.tracsactiontracker.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.BudgetActivity
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.TracsactionTrackerApplication
import com.example.tracsactiontracker.adapter.TransactionAdapter
import com.example.tracsactiontracker.data.BalanceManager
import com.example.tracsactiontracker.data.TransactionDao
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvMonthlyBudget: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionDao: TransactionDao
    private lateinit var balanceManager: BalanceManager
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance)
        tvMonthlyBudget = view.findViewById(R.id.tvMonthlyBudget)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Initialize managers
        transactionDao = (requireActivity().application as TracsactionTrackerApplication).database.transactionDao()
        balanceManager = BalanceManager(requireContext())

        // Setup RecyclerView
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle transaction click
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }

        // Setup Set Budget button
        view.findViewById<MaterialButton>(R.id.btnAddBudget)?.setOnClickListener {
            startActivity(Intent(requireContext(), BudgetActivity::class.java))
        }

        // Observe balance changes
        balanceManager.totalBalance.observe(viewLifecycleOwner, Observer { balance ->
            tvTotalBalance.text = currencyFormat.format(balance)
        })

        balanceManager.monthlyBudget.observe(viewLifecycleOwner, Observer { budget ->
            tvMonthlyBudget.text = currencyFormat.format(budget)
        })

        balanceManager.totalIncome.observe(viewLifecycleOwner, Observer { income ->
            tvTotalIncome.text = currencyFormat.format(income)
        })

        balanceManager.totalExpenses.observe(viewLifecycleOwner, Observer { expenses ->
            tvTotalExpenses.text = currencyFormat.format(expenses)
        })

        // Load recent transactions
        loadRecentTransactions()
    }

    private fun loadRecentTransactions() {
        // Load recent transactions from database
        // This will be implemented when we have the database setup
    }

    override fun onResume() {
        super.onResume()
        loadRecentTransactions()
    }
} 