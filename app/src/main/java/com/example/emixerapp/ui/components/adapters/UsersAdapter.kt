package com.example.emixerapp.ui.components.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.data.model.UserModel
import com.example.mvvmapp.databinding.AdapterUserBinding

class UsersAdapter(private val dataSet: ArrayList<UserModel>) :
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

        viewHolder.binding(currentItem.name)

    }
    override fun getItemCount() = dataSet.size

    // ViewHolder class to hold the view binding
    class ViewHolder(private val binding: AdapterUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun binding(user: String) {
            binding.Userdata.text = user
        }
    }
}
