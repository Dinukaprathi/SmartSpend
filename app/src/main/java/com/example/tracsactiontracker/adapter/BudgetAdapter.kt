package com.example.tracsactiontracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.data.BudgetEntity
import java.text.SimpleDateFormat
import java.util.Locale

class BudgetAdapter(
    private val onDeleteClick: (BudgetEntity) -> Unit
) : ListAdapter<BudgetEntity, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BudgetViewHolder(
        itemView: View,
        private val onDeleteClick: (BudgetEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvBudgetAmount: TextView = itemView.findViewById(R.id.tvBudgetAmount)
        private val tvBudgetDate: TextView = itemView.findViewById(R.id.tvBudgetDate)
        private val btnDeleteBudget: ImageButton = itemView.findViewById(R.id.btnDeleteBudget)

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(budget: BudgetEntity) {
            val symbol = when (budget.currency) {
                "USD" -> "$"
                "LKR" -> "Rs"
                "EUR" -> "€"
                "AED" -> "د.إ"
                else -> "$"
            }
            tvBudgetAmount.text = "$symbol${String.format("%.2f", budget.amount)}"
            tvBudgetDate.text = "${dateFormat.format(budget.startDate)} - ${dateFormat.format(budget.endDate)}"

            btnDeleteBudget.setOnClickListener {
                onDeleteClick(budget)
            }
        }
    }

    private class BudgetDiffCallback : DiffUtil.ItemCallback<BudgetEntity>() {
        override fun areItemsTheSame(oldItem: BudgetEntity, newItem: BudgetEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BudgetEntity, newItem: BudgetEntity): Boolean {
            return oldItem == newItem
        }
    }
} 