package com.example.emixerapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mvvmapp.databinding.FragmentManageUserBinding

/**
 * A simple [Fragment] subclass.
 * Use the [ManageUser.newInstance] factory method to
 * create an instance of this fragment.
 */

class ManageUser : Fragment() {

    private lateinit var binding: FragmentManageUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentManageUserBinding.inflate(inflater, container, false)

        return binding.root
    }

}