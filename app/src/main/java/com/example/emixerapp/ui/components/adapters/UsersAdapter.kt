package com.example.emixerapp.ui.components.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.IconManager
import com.example.emixerapp.data.model.UserModel
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.AdapterUserBinding


class UsersAdapter(var dataSet: ArrayList<UserModel>) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    var onItemClick: ((UserModel) -> Unit)? = null

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
    inner class ViewHolder(val binding: AdapterUserBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(dataSet[adapterPosition]) // Call onItemClick with the UserModel
            }
        }        fun binding(user: UserModel) {
            binding.userNameTextView.text = user.name

            // Use IconManager to get the drawable
            val drawableResource = IconManager.getDrawableResource(user.iconIndex)
            binding.userIconImageView.setImageResource(drawableResource)

            val iconSize = 60 // Consider making this a constant
            val layoutParams = binding.userIconImageView.layoutParams
            layoutParams.width = iconSize
            layoutParams.height = iconSize
            binding.userIconImageView.layoutParams = layoutParams
        }
    }
}
