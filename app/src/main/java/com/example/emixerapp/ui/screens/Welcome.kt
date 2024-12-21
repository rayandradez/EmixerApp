package com.example.emixerapp.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.UsersAdapter
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.FragmentWelcomeBinding
import androidx.recyclerview.widget.LinearLayoutManager

class Welcome : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    lateinit var myRecyclerUser: RecyclerView
    var userList = ArrayList<UserModel>()
    lateinit var adapterUserList: UsersAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        myRecyclerUser = binding.recyclerViewUser
        userList = ArrayList<UserModel>()
        userList.add(UserModel("Rayanne", 3, 0, 0, 0))

        adapterUserList = UsersAdapter(userList)

        myRecyclerUser.adapter = adapterUserList
        myRecyclerUser.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        myRecyclerUser.setHasFixedSize(true)

        binding.ManageUser.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcome_to_manageUser)
        }
        binding.NewUser.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcome_to_addUser2)
        }

        return binding.root
    }

}