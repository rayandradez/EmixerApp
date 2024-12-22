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
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.IconsAdapter
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.mvvmapp.databinding.FragmentAddUserBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ManageUser.newInstance] factory method to
 * create an instance of this fragment.
 */

class AddUser : Fragment() {

    private lateinit var binding: FragmentAddUserBinding
    private lateinit var viewModel: MainViewModel
    private var selectedIconIndex = 0 //keep track of the selected icon

    lateinit var myRecyclerIcon: RecyclerView
    var iconList = ArrayList<String>()
    lateinit var adapterIconList: IconsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddUserBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // Get ViewModel



        myRecyclerIcon = binding.recyclerViewIcons
        iconList = ArrayList<String>()
        iconList.add("HeadSet")
        iconList.add("Trombone")
        iconList.add("Safona")
        iconList.add("AcousticViolon")

        adapterIconList = IconsAdapter(iconList)

        myRecyclerIcon.adapter = adapterIconList
        myRecyclerIcon.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        myRecyclerIcon.setHasFixedSize(true)

        adapterIconList.onItemClick  = { position ->
            selectedIconIndex = position
            // Log.e("AddUser", "CurrentPosition: " + position)
           // binding.editNewName.setText("My new position: " + position)
        }

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