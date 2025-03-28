package com.example.emixerapp.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.ui.components.adapters.UsersAdapter
import com.reaj.emixer.databinding.FragmentWelcomeBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.reaj.emixer.R
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import kotlinx.coroutines.launch

class Welcome : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var viewModel: MainViewModel

    // Referências para a RecyclerView e o adaptador.  Declaradas aqui para melhor organização.
    lateinit var myRecyclerUser: RecyclerView
    lateinit var adapterUserList: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla a view do layout.
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        // Obtém uma instância do ViewModel usando o ViewModelProvider.
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]


        // Configura a RecyclerView.
        myRecyclerUser = binding.recyclerViewUser
        adapterUserList = UsersAdapter(arrayListOf())
        myRecyclerUser.adapter = adapterUserList
        myRecyclerUser.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        myRecyclerUser.setHasFixedSize(true) // Otimização de performance.

        // Define o listener de clique para os itens da RecyclerView.
        adapterUserList.onItemClick = { user ->
            // Define o usuário atual no ViewModel e navega para a tela de detalhes do usuário.
            viewModel.setCurrentUser(user)
            findNavController().navigate(WelcomeDirections.actionWelcomeToUserPage(UserModel()))
        }

        binding.addNewUserButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_addUser)
        }


        // Coleta as mudanças no estado da UI do ViewModel e atualiza a RecyclerView usando DiffUtil para melhor performance.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    val newList = uiState.usersList
                    val diffResult = DiffUtil.calculateDiff(UsersDiffCallback(adapterUserList.dataSet, newList))
                    adapterUserList.dataSet.clear()
                    adapterUserList.dataSet.addAll(newList)
                    diffResult.dispatchUpdatesTo(adapterUserList)

                    if (newList.isEmpty()) {
                        binding.txtwelcome.text = getString(R.string.no_profiles_message)
                        binding.recyclerViewUser.isVisible = false
                    } else {
                        binding.txtwelcome.text = getString(R.string.select_profile_header_text)
                        binding.recyclerViewUser.isVisible = true
                    }
                }
            }
        }

        // Retorna a view raiz.
        return binding.root

    }

        // Callback para calcular as diferenças entre listas de usuários para atualização eficiente da RecyclerView.
        class UsersDiffCallback(private val oldList: List<UserModel>, private val newList: List<UserModel>) : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        }

}