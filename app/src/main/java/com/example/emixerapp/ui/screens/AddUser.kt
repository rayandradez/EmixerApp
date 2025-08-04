package com.reaj.emixer.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.content.Context
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reaj.emixer.IconManager
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.ui.components.adapters.IconsAdapter
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.databinding.FragmentAddUserBinding
import androidx.activity.OnBackPressedCallback
import android.widget.Toast

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density + 0.5f).toInt()
}

class AddUser : Fragment() {

    private lateinit var binding: FragmentAddUserBinding
    private lateinit var viewModel: MainViewModel
    private var selectedIconDrawableResId: Int? = null
    private var hasChanges = false


    lateinit var myRecyclerIcon: RecyclerView
    lateinit var adapterIconList: IconsAdapter
    private val args: AddUserArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddUserBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        myRecyclerIcon = binding.recyclerViewIcons
        adapterIconList = IconsAdapter(IconManager.iconDrawables.toCollection(ArrayList()))
        myRecyclerIcon.adapter = adapterIconList
        myRecyclerIcon.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        adapterIconList.onItemClick = { newSelectedPosition ->
            selectedIconDrawableResId = adapterIconList.getSelectedIconDrawableResId()
            hasChanges = true
        }

        args.selectedUser?.let { user ->
            binding.editNewName.setText(user.name)
            selectedIconDrawableResId = user.iconIndex
            adapterIconList.setSelectedIconByDrawableResId(user.iconIndex)

            updateDeleteAndCancelButtonVisibility(true)
        } ?: run {
            updateDeleteAndCancelButtonVisibility(false)
            if (adapterIconList.itemCount > 0) {
                adapterIconList.setSelectedIconByDrawableResId(IconManager.iconDrawables[0])
                selectedIconDrawableResId = IconManager.iconDrawables[0]
            }
        }

        binding.recyclerViewIcons.post {
            centerRecyclerViewItems()
        }

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

        binding.BtndeleteUser.setOnClickListener() {
            showDeleteProfileDialog()
        }

        binding.btnBack.setOnClickListener {
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

    private fun updateDeleteAndCancelButtonVisibility(isEditingExistingUser: Boolean) {
        binding.BtndeleteUser.visibility = if (isEditingExistingUser) View.VISIBLE else View.GONE
        binding.BtnCancelUser.visibility = if (isEditingExistingUser) View.VISIBLE else View.GONE
    }

    private fun saveUser() {
        val userName = binding.editNewName.text.toString().trim()
        val currentSelectedIconResId = adapterIconList.getSelectedIconDrawableResId()

        if (userName.isEmpty()) {
            Toast.makeText(requireContext(), "User name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentSelectedIconResId == null) {
            Toast.makeText(requireContext(), "Please select an icon", Toast.LENGTH_SHORT).show()
            return
        }

        val user = args.selectedUser ?: UserModel(
            id = java.util.UUID.randomUUID().toString(),
            name = userName,
            iconIndex = currentSelectedIconResId,
            0, 0, 0, 50,0
        )
        user.name = userName
        user.iconIndex = currentSelectedIconResId
        viewModel.updateUser(user)
        findNavController().navigateUp()
        binding.editNewName.text?.clear()
        selectedIconDrawableResId = null
        hasChanges = false
    }

    private fun deleteUser() {
        args.selectedUser?.let { userToDelete ->
            viewModel.deleteUser(userToDelete)
            findNavController().navigateUp()
            binding.editNewName.text?.clear()
            selectedIconDrawableResId = null
            hasChanges = false
        } ?: run {
            Toast.makeText(requireContext(), "No user selected to delete", Toast.LENGTH_SHORT).show()
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

    private fun centerRecyclerViewItems() {
        val recyclerViewWidth = binding.recyclerViewIcons.width
        // Largura total de um item de carro (80dp de largura + 2*6dp de margem = 92dp total)
        val singleItemTotalWidth = 80.dpToPx(requireContext()) + 2 * 6.dpToPx(requireContext())

        val numberOfItems = adapterIconList.itemCount
        val totalItemsContentWidth = numberOfItems * singleItemTotalWidth

        // Obter os paddings originais definidos no XML para preservar se o conteúdo preencher
        val originalPaddingStart = binding.recyclerViewIcons.paddingStart
        val originalPaddingEnd = binding.recyclerViewIcons.paddingEnd

        if (totalItemsContentWidth < recyclerViewWidth) {
            val extraSpace = recyclerViewWidth - totalItemsContentWidth
            val padding = extraSpace / 2
            binding.recyclerViewIcons.setPadding(padding, 0, padding, 0)
            binding.recyclerViewIcons.clipToPadding = false // Permite que os itens desenhem na área de padding
        } else {
            // Se o conteúdo preencher ou exceder a largura do RecyclerView, reverter para o padding original
            binding.recyclerViewIcons.setPadding(originalPaddingStart, 0, originalPaddingEnd, 0)
            binding.recyclerViewIcons.clipToPadding = true // Reverter se necessário
        }
    }

    private fun showDeleteProfileDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Profile?")
        builder.setMessage("Do you want to delete this profile? This can't be undone")
        builder.setPositiveButton("Delete") { _, _ ->
            deleteUser()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
