package com.example.emixerapp.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.UsersAdapter
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.FragmentManageUserBinding
import kotlinx.coroutines.launch


class ManageUser : Fragment() {

    private lateinit var binding: FragmentManageUserBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsersAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageUserBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerViewUser // Get reference to RecyclerView in your layout
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)


        adapter = UsersAdapter(ArrayList()) // Initialize the adapter only ONCE
        adapter.onItemClick = { selectedUser -> // Set the listener immediately after adapter creation
            val action = ManageUserDirections.actionManageUserToAddUser(selectedUser)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    val newList = uiState.usersList
                    if (newList.isNotEmpty()) {
                        val diffResult = DiffUtil.calculateDiff(UsersDiffCallback(adapter.dataSet, newList))
                        adapter.dataSet.clear()
                        adapter.dataSet.addAll(newList)
                        diffResult.dispatchUpdatesTo(adapter)
                    } else {
                        // Handle empty list (e.g., show a message)
                    }
                }
            }
        }

        binding.addNewUserButton.setOnClickListener {
            findNavController().navigate(ManageUserDirections.actionManageUserToAddUser(null)) // Explicitly pass null for new user
        }


        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}

class UsersDiffCallback(private val oldList: List<UserModel>, private val newList: List<UserModel>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
}