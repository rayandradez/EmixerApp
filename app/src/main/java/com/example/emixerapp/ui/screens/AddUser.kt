package com.example.emixerapp.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.IconManager
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.IconsAdapter
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.mvvmapp.databinding.FragmentAddUserBinding


/**
 * Fragment for adding new users.
 */
class AddUser : Fragment() {

    private lateinit var binding: FragmentAddUserBinding
    private lateinit var viewModel: MainViewModel
    private var selectedIconIndex = 0 //keep track of the selected icon

    // RecyclerView for displaying icons
    lateinit var myRecyclerIcon: RecyclerView
    // Adapter for the icon RecyclerView
    lateinit var adapterIconList: IconsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddUserBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // Get ViewModel

        // Initialize RecyclerView and Adapter
        myRecyclerIcon = binding.recyclerViewIcons
        adapterIconList = IconsAdapter(IconManager.iconDrawables.toCollection(ArrayList()))
        myRecyclerIcon.adapter = adapterIconList
        myRecyclerIcon.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        myRecyclerIcon.setHasFixedSize(true)

        // Set click listener for icon selection
        adapterIconList.onItemClick  = { position ->
            selectedIconIndex = position
        }

        // Handle Save button click
        binding.BtnSaveUser.setOnClickListener {
            saveUser()
        }

        return binding.root
    }

    private fun saveUser() {
        val userName = binding.editNewName.text.toString()
        if (userName.isNotEmpty()) {
            val newUser = UserModel(userName, selectedIconIndex, 0, 0, 0)
            viewModel.addUser(newUser)
            findNavController().navigateUp() // Go back to the previous screen
            binding.editNewName.text?.clear() // Clear the EditText
            binding.editNewName.text?.clear() // Clear the EditText
            selectedIconIndex = 0 // Reset the selected icon index
        } else {
            Log.e("AddUser", "User name cannot be empty")
        }
    }


}