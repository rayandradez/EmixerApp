package com.example.emixerapp.ui.components.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reaj.emixer.R

class SimpleListAdapter(private val items: List<String>) : RecyclerView.Adapter<SimpleListAdapter.SimpleViewHolder>() {

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple, parent, false)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.itemName.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}
