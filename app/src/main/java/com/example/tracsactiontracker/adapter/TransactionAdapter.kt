package com.example.tracsactiontracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.data.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onItemClick: (TransactionEntity) -> Unit
) : ListAdapter<TransactionEntity, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(transaction: TransactionEntity) {
            tvTitle.text = transaction.description
            val symbol = when (transaction.currency) {
                "USD" -> "$"
                "LKR" -> "Rs"
                "EUR" -> "€"
                "AED" -> "د.إ"
                else -> "$"
            }
            tvAmount.text = String.format("%s%.2f", symbol, transaction.amount)
            tvCategory.text = transaction.category
            tvDate.text = dateFormat.format(Date(transaction.date))
            
            // Set text color based on transaction type
            tvAmount.setTextColor(
                if (transaction.type == "INCOME") {
                    itemView.context.getColor(R.color.income_green)
                } else {
                    itemView.context.getColor(R.color.expense_red)
                }
            )

            itemView.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity): Boolean {
            return oldItem.description == newItem.description &&
                    oldItem.amount == newItem.amount &&
                    oldItem.category == newItem.category &&
                    oldItem.date == newItem.date &&
                    oldItem.type == newItem.type
        }
    }
} 