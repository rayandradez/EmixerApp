package com.example.emixerapp.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.ui.components.adapters.IconsAdapter
import com.example.mvvmapp.databinding.FragmentAddUserBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ManageUser.newInstance] factory method to
 * create an instance of this fragment.
 */

class AddUser : Fragment() {

    private lateinit var binding: FragmentAddUserBinding

    lateinit var myRecyclerIcon: RecyclerView
    var iconList = ArrayList<String>()
    lateinit var adapterIconList: IconsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddUserBinding.inflate(inflater, container, false)

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
            Log.e("AddUser", "CurrentPosition: " + position)
            binding.editNewName.setText("My new position: " + position)
        }

        return binding.root
    }

}