package com.example.emixerapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.emixerapp.databinding.FragmentWelcomeBinding

class Welcome : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        binding.User1.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcome_to_userPage)
        }
        binding.ManageUser.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcome_to_manageUser)
        }
        binding.NewUser.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcome_to_addUser2)
        }

        return binding.root
    }

}