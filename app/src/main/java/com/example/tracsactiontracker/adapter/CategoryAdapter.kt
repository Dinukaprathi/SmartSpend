package com.example.tracsactiontracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import com.example.tracsactiontracker.data.Category

class CategoryAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<String, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private val categories = mutableListOf<Category>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
    }

    fun getCategories(): List<Category> = categories.toList()

    fun updateCategories(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        submitList(newCategories.map { it.name })
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)

        fun bind(category: String) {
            tvCategory.text = category
            itemView.setOnClickListener { onItemClick(category) }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
} 