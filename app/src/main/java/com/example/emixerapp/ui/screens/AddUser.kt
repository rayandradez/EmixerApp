package com.example.emixerapp.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mvvmapp.databinding.FragmentAddUserBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ManageUser.newInstance] factory method to
 * create an instance of this fragment.
 */

class AddUser : Fragment() {

    private lateinit var binding: FragmentAddUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddUserBinding.inflate(inflater, container, false)

        return binding.root
    }

}