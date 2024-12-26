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
 * Fragmento para adicionar novos usuários ou editar usuários existentes.
 */
class AddUser : Fragment() {

    private lateinit var binding: FragmentAddUserBinding
    private lateinit var viewModel: MainViewModel
    private var selectedIconIndex = 0 // Índice do ícone selecionado.
    private var hasChanges = false  // Indica se há mudanças não salvas.


    // RecyclerView para exibir os ícones.
    lateinit var myRecyclerIcon: RecyclerView
    // Adaptador para a RecyclerView de ícones.
    lateinit var adapterIconList: IconsAdapter
    // Obtém os argumentos passados para o fragmento.
    private val args: AddUserArgs by navArgs() // Get arguments


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout do fragmento.
        binding = FragmentAddUserBinding.inflate(inflater, container, false)
        // Obtém uma instância do ViewModel.
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        // Inicializa a RecyclerView e o adaptador.
        myRecyclerIcon = binding.recyclerViewIcons
        adapterIconList = IconsAdapter(IconManager.iconDrawables.toCollection(ArrayList()))
        myRecyclerIcon.adapter = adapterIconList
        myRecyclerIcon.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        myRecyclerIcon.setHasFixedSize(true)  // Otimização de performance.

        // Define o listener de clique para seleção de ícone.
        adapterIconList.onItemClick = { position ->
            selectedIconIndex = position // Atualiza o índice do ícone selecionado.
            hasChanges = true // Define hasChanges como true quando o ícone muda.
            updateIconDisplay() // Atualiza a exibição do ícone.
        }

        // Preenche o formulário com os dados do usuário, se houver.
        args.selectedUser?.let { user ->
            binding.editNewName.setText(user.name)
            selectedIconIndex = user.iconIndex
            updateIconDisplay()
        } ?: run {
            // Lidar com o caso em que nenhum usuário foi selecionado. O formulário deve estar vazio.
            // Isso já é tratado no método saveUser(), mas esta seção é importante para a UI.
        }

        // Define o listener de clique para o botão "Salvar".
        binding.BtnSaveUser.setOnClickListener {
            saveUser()  // Salva as informações do usuário.
        }

        // Define o listener de clique para o botão "Cancelar".
        binding.BtnCancelUser.setOnClickListener {
            // Mostra um diálogo para confirmar o descarte das alterações, se houver.
            if (hasChanges) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()  // Navega para a tela anterior.
            }
        }

        // Adiciona um listener para detectar mudanças no campo de nome.
        binding.editNewName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasChanges = true  // Define hasChanges como true quando o texto muda.
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Adiciona um callback para o botão de voltar do sistema.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Mostra um diálogo para confirmar o descarte das alterações, se houver.
                if (hasChanges) {
                    showDiscardChangesDialog()
                } else {
                    findNavController().navigateUp()  // Navega para a tela anterior.
                }
            }
        })
    }

    // Atualiza a exibição do ícone selecionado.
    private fun updateIconDisplay() {
        val drawableResource = IconManager.getDrawableResource(selectedIconIndex)
        binding.userIconImageView.setImageResource(drawableResource)
    }

    // Salva as informações do usuário.
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

    // Mostra um diálogo para confirmar o descarte das alterações.
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


