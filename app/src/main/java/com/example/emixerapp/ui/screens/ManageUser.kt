package com.example.emixerapp.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.adapters.UsersAdapter
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.FragmentManageUserBinding
import kotlinx.coroutines.launch


class ManageUser : Fragment() {

    private lateinit var binding: FragmentManageUserBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsersAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout do fragmento.
        binding = FragmentManageUserBinding.inflate(inflater, container, false)
        // Obtém referências para a RecyclerView e o ViewModel.
        recyclerView = binding.recyclerViewUser
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        // Inicializa o adaptador apenas uma vez.
        adapter = UsersAdapter(ArrayList())
        // Define o listener de clique para os itens da lista de usuários.
        adapter.onItemClick = { selectedUser ->
            // Navega para a tela de adicionar/editar usuário, passando o usuário selecionado como argumento.
            val action = ManageUserDirections.actionManageUserToAddUser(selectedUser)
            findNavController().navigate(action)
        }
        // Configura a RecyclerView.
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)  // Otimiza o desempenho.

        // Observa as mudanças no estado da UI do ViewModel e atualiza a RecyclerView usando DiffUtil.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    val newList = uiState.usersList
                    if (newList.isNotEmpty()) {
                        // Calcula as diferenças entre a lista antiga e a nova usando DiffUtil para atualizações eficientes.
                        val diffResult = DiffUtil.calculateDiff(UsersDiffCallback(adapter.dataSet, newList))
                        adapter.dataSet.clear()
                        adapter.dataSet.addAll(newList)
                        diffResult.dispatchUpdatesTo(adapter)
                    } else {
                        // Lidar com a lista vazia (exibir uma mensagem, etc.)  - Implementação a ser adicionada conforme necessário.
                    }
                }
            }
        }

        // Define o listener de clique para o botão "Adicionar Novo Usuário".
        binding.addNewUserButton.setOnClickListener {
            // Navega para a tela de adicionar usuário, passando null como argumento para indicar um novo usuário.
            findNavController().navigate(ManageUserDirections.actionManageUserToAddUser(null)) // Explicitly pass null for new user
        }

        // Define o listener de clique para o botão "Voltar".
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()  // Navega para a tela anterior.
        }
        return binding.root
    }
}

// Callback para calcular as diferenças entre listas de usuários para atualização eficiente da RecyclerView.
class UsersDiffCallback(private val oldList: List<UserModel>, private val newList: List<UserModel>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
}