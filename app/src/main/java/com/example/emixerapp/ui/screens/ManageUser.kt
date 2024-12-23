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


        adapter = UsersAdapter(ArrayList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) // Vertical list
        recyclerView.setHasFixedSize(true)

        // Observe the ViewModel's userListState
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    adapter.dataSet.clear()
                    adapter.dataSet.addAll(uiState.usersList)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        // Set click listener for each item in the RecyclerView
        adapter.onItemClick = { position ->
            val selectedUser = adapter.dataSet.getOrNull(position)

            val action = if (selectedUser != null) {
                ManageUserDirections.actionManageUserToAddUser3(selectedUser)
            } else {
                ManageUserDirections.actionManageUserToAddNewUser()
            }
            findNavController().navigate(action)
        }

        //Handle Add New User button click (removed duplicate listener)
        binding.addNewUserButton.setOnClickListener {
            findNavController().navigate(R.id.action_manageUser_to_addNewUser) //Use the correct action ID
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    // Function to fetch the user list (replace with your data source)
//    private fun fetchUserList(): ArrayList<UserModel> {
//        //  Implement your data fetching logic here
//        // This could involve getting data from a ViewModel, database, or network call.
//        // For now, we'll use a placeholder list.  Replace this with your actual data loading.
//
//    }
}
