package com.example.tracsactiontracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsactiontracker.R
import java.util.Locale

class CategorySummaryAdapter(
    private var categories: List<Pair<String, Double>>
) : RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, amount) = categories[position]
        holder.tvCategory.text = category
        holder.tvAmount.text = String.format(Locale.getDefault(), "$%.2f", amount)
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<Pair<String, Double>>) {
        categories = newCategories
        notifyDataSetChanged()
    }
} 