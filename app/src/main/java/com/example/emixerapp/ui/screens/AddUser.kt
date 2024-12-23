package com.example.emixerapp.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.IconManager
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.IconsAdapter
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.mvvmapp.databinding.FragmentAddUserBinding
import androidx.activity.OnBackPressedCallback



/**
 * Fragment for adding new users.
 */
class AddUser : Fragment() {

    private lateinit var binding: FragmentAddUserBinding
    private lateinit var viewModel: MainViewModel
    private var selectedIconIndex = 0 //keep track of the selected icon

    private var hasChanges = false


    // RecyclerView for displaying icons
    lateinit var myRecyclerIcon: RecyclerView
    // Adapter for the icon RecyclerView
    lateinit var adapterIconList: IconsAdapter
    private val args: AddUserArgs by navArgs() // Get arguments


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
        adapterIconList.onItemClick = { position ->
            selectedIconIndex = position // Update the index in the fragment
            hasChanges = true // Set hasChanges to true when icon changes
            updateIconDisplay() // Update the display
        }

        args.selectedUser?.let { user ->
            // Pre-fill the form with existing user data
            binding.editNewName.setText(user.name)
            selectedIconIndex = user.iconIndex
            updateIconDisplay()
        } ?: run {
            //Handle the case where no user was selected.  The form should be empty.
            //This is already handled in your saveUser() method, but this section is important for the UI
        }

        // Handle Save button click
        binding.BtnSaveUser.setOnClickListener {
            saveUser()
        }

        binding.BtnCancelUser.setOnClickListener {
            if (hasChanges) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()
            }
        }


        binding.editNewName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasChanges = true
            }
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasChanges) {
                    showDiscardChangesDialog()
                } else {
                    findNavController().navigateUp()
                }
            }
        })
    }


    private fun updateIconDisplay() {
        val drawableResource = IconManager.getDrawableResource(selectedIconIndex)
        binding.userIconImageView.setImageResource(drawableResource)
    }

    private fun saveUser() {
        val userName = binding.editNewName.text.toString()
        if (userName.isNotEmpty()) {
            val user = args.selectedUser ?: UserModel(name = userName, iconIndex = selectedIconIndex)
            user.name = userName
            user.iconIndex = selectedIconIndex
            viewModel.updateUser(user)
            findNavController().navigateUp()
            binding.editNewName.text?.clear()
            selectedIconIndex = 0
        } else {
            Log.e("AddUser", "User name cannot be empty")
        }
    }

    private fun showDiscardChangesDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Discard Changes?")
        builder.setMessage("You have unsaved changes. Do you want to discard them?")
        builder.setPositiveButton("Discard") { _, _ ->
            findNavController().navigateUp()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()

}
}


