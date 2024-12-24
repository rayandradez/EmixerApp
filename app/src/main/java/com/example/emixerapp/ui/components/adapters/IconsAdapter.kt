package com.example.emixerapp.ui.components.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.AdapterIconsBinding

class IconsAdapter(private val dataSet: ArrayList<Int>) :
    RecyclerView.Adapter<IconsAdapter.ViewHolder>() {
    var onItemClick: ((Int) -> Unit)? = null
    private var selectedPosition = -1 // Track the currently selected position

        inner class ViewHolder(val binding: AdapterIconsBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        selectedPosition = if (selectedPosition == position) -1 else position // Toggle selection
                        notifyDataSetChanged()
                        onItemClick?.invoke(position) // Pass the position to the callback
                    }
                }
            }

            fun bind(iconResource: Int, isSelected: Boolean) {
                binding.iconImageView.setImageResource(iconResource)
                binding.iconImageView.setBackgroundResource(
                    if (isSelected) R.drawable.circle_selected_icon else R.drawable.transparent_background
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = AdapterIconsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(dataSet[position], position == selectedPosition) // Call the bind function here
        }

        override fun getItemCount(): Int = dataSet.size
    }
