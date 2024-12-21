package com.example.emixerapp.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.UsersAdapter
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.FragmentManageUserBinding


class ManageUser : Fragment() {

    private lateinit var binding: FragmentManageUserBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsersAdapter
    private lateinit var userList: ArrayList<UserModel> // Make userList a property


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageUserBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerViewUser // Get reference to RecyclerView in your layout


        // Fetch the user list (replace with your actual data fetching logic)
        userList = fetchUserList() //  Get users from ViewModel or repository

        adapter = UsersAdapter(userList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) // Vertical list
        recyclerView.setHasFixedSize(true)


        binding.addNewUserButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_manageUser_to_AddUser)
        }

        binding.backButton.setOnClickListener {
            it.findNavController().navigateUp()
        }

        return binding.root
    }

    // Function to fetch the user list (replace with your data source)
    private fun fetchUserList(): ArrayList<UserModel> {
        //  Implement your data fetching logic here
        // This could involve getting data from a ViewModel, database, or network call.
        // For now, we'll use a placeholder list.  Replace this with your actual data loading.
        val list = ArrayList<UserModel>()
        list.add(UserModel("Rayanne", 1, 0, 0, 0))
        list.add(UserModel("Another User", 2, 1, 1, 1))
        return list
    }
}
