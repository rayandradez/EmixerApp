package com.reaj.emixer.ui.components.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reaj.emixer.R
import com.reaj.emixer.databinding.AdapterIconsBinding

class IconsAdapter(private val dataSet: ArrayList<Int>) :
    RecyclerView.Adapter<IconsAdapter.ViewHolder>() {

    var onItemClick: ((Int) -> Unit)? = null
    var selectedPosition = -1

    inner class ViewHolder(val binding: AdapterIconsBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val currentAdapterPosition = adapterPosition
                if (currentAdapterPosition != RecyclerView.NO_POSITION) {
                    val previousSelectedPosition = selectedPosition

                    selectedPosition = if (selectedPosition == currentAdapterPosition) {
                        RecyclerView.NO_POSITION
                    } else {
                        currentAdapterPosition
                    }

                    if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelectedPosition)
                    }
                    notifyItemChanged(currentAdapterPosition)

                    onItemClick?.invoke(selectedPosition)
                }
            }
        }

        fun bind(iconResource: Int, isSelected: Boolean) {
            binding.iconImageView.setImageResource(iconResource)
            binding.selectionBorder.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterIconsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = dataSet.size

    fun setSelectedIconByDrawableResId(drawableResId: Int) {
        val index = dataSet.indexOf(drawableResId)
        if (index != -1) {
            val previousSelectedPosition = selectedPosition
            selectedPosition = index
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition)
            }
            notifyItemChanged(selectedPosition)
        }
    }

    fun getSelectedIconDrawableResId(): Int? {
        return if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < dataSet.size) {
            dataSet[selectedPosition]
        } else {
            null
        }
    }
}
