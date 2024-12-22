package com.example.emixerapp.ui.components.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.data.model.UserModel
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.AdapterUserBinding

class UsersAdapter(val dataSet: ArrayList<UserModel>) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterUserBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]

        viewHolder.binding(currentItem)

    }
    override fun getItemCount() = dataSet.size

    // ViewHolder class to hold the view binding
    class ViewHolder(private val binding: AdapterUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun binding(user: UserModel) {
            binding.userNameTextView.text = user.name

            // Array of drawable resources (replace with your actual drawable names)
            val iconDrawables = arrayOf(
                R.drawable.car,
                R.drawable.rocket,
                R.drawable.ic_launcher_foreground,
                R.drawable.car,
                R.drawable.rocket
            )

            // Set the icon based on the iconIndex
            val iconIndex = user.iconIndex
            if (iconIndex in iconDrawables.indices) {
                binding.userIconImageView.setImageResource(iconDrawables[iconIndex])
            } else {
                // Handle invalid iconIndex (e.g., display a default icon)
                binding.userIconImageView.setImageResource(R.drawable.ic_launcher_foreground)
            }

            val textSize = binding.userNameTextView.textSize
            val iconSize = textSize.toInt() // Convert to integer for setting dimensions
            val layoutParams = binding.userIconImageView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = iconSize
            layoutParams.height = iconSize
            binding.userIconImageView.layoutParams = layoutParams
        }
    }
}
