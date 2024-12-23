package com.example.emixerapp.ui.components.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmapp.databinding.AdapterIconsBinding

class IconsAdapter(private val dataSet: ArrayList<Int>) :
    RecyclerView.Adapter<IconsAdapter.ViewHolder>() {
    var onItemClick: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterIconsBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]
        viewHolder.binding(currentItem)
        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(position)
        }

    }
    override fun getItemCount() = dataSet.size

    // ViewHolder class to hold the view binding
    class ViewHolder(private val binding: AdapterIconsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun binding(icon: Int) {
            binding.imgIcon.setImageResource(icon) // Set the image resource
            binding.imgIcon.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
        }
    }
}
