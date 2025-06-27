package com.example.tracsactiontracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import java.util.Locale

data class CategoryAnalysisItem(
    val category: String,
    val amount: Double,
    val percentage: Double
)

class CategoryAnalysisAdapter : ListAdapter<CategoryAnalysisItem, CategoryAnalysisAdapter.CategoryAnalysisViewHolder>(CategoryAnalysisDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAnalysisViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_analysis, parent, false)
        return CategoryAnalysisViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryAnalysisViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class CategoryAnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCategoryAmount: TextView = itemView.findViewById(R.id.tvCategoryAmount)
        private val tvCategoryPercentage: TextView = itemView.findViewById(R.id.tvCategoryPercentage)

        fun bind(item: CategoryAnalysisItem) {
            tvCategoryName.text = item.category
            tvCategoryAmount.text = String.format(Locale.getDefault(), "$%.2f", item.amount)
            tvCategoryPercentage.text = String.format(Locale.getDefault(), "%.1f%%", item.percentage)
        }
    }

    private class CategoryAnalysisDiffCallback : DiffUtil.ItemCallback<CategoryAnalysisItem>() {
        override fun areItemsTheSame(oldItem: CategoryAnalysisItem, newItem: CategoryAnalysisItem): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategoryAnalysisItem, newItem: CategoryAnalysisItem): Boolean {
            return oldItem == newItem
        }
    }
} 