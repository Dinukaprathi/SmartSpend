package com.example.tracsactiontracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.data.TransactionEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<TransactionEntity, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)

        fun bind(transaction: TransactionEntity) {
            val symbol = when (transaction.currency) {
                "USD" -> "$"
                "LKR" -> "Rs"
                "EUR" -> "€"
                "AED" -> "د.إ"
                else -> "$"
            }
            tvAmount.text = String.format("%s%.2f", symbol, transaction.amount)
            tvCategory.text = transaction.category
            tvDate.text = dateFormat.format(transaction.date)
            tvType.text = transaction.type

            // Set text color based on transaction type
            val colorRes = when (transaction.type) {
                "INCOME" -> R.color.green
                "EXPENSE" -> R.color.red
                else -> R.color.black
            }
            tvAmount.setTextColor(itemView.context.getColor(colorRes))
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity): Boolean {
            return oldItem == newItem
        }
    }
} 