package com.example.emixerapp.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.UsersAdapter
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.FragmentWelcomeBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Welcome : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var viewModel: MainViewModel

    lateinit var myRecyclerUser: RecyclerView
    var userList = ArrayList<UserModel>()
    lateinit var adapterUserList: UsersAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        myRecyclerUser = binding.recyclerViewUser
        adapterUserList = UsersAdapter(arrayListOf())
        myRecyclerUser.adapter = adapterUserList
        myRecyclerUser.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        myRecyclerUser.setHasFixedSize(true)

        binding.ManageUser.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcome_to_manageUser)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userListState.collect { uiState ->
                    Log.d("WelcomeFragment", "Received userListState update: ${uiState.usersList}")
                    adapterUserList.dataSet.clear()
                    adapterUserList.dataSet.addAll(uiState.usersList)
                    adapterUserList.notifyDataSetChanged()
                }
            }
        }

        return binding.root
    }

}