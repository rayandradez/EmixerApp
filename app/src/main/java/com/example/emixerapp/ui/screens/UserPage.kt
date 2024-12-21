package com.example.emixerapp.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mvvmapp.databinding.FragmentUserPageBinding

class UserPage : Fragment() {

    private lateinit var binding: FragmentUserPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserPageBinding.inflate(inflater, container, false)

        return binding.root    }

}