package com.example.emixerapp.ui.screens

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

// Importar os managers necessários
import com.example.emixerapp.manager.AidlServiceManager
import com.example.emixerapp.manager.AudioManager

class Welcome : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    lateinit var myRecyclerUser: RecyclerView
    lateinit var adapterUserList: UsersAdapter

    // Declarar os managers para o serviço AIDL
    private lateinit var aidlServiceManager: AidlServiceManager
    private lateinit var audioManager: AudioManager // Será inicializado após a conexão do serviço

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        // Inicializar AidlServiceManager aqui, pois precisa de um Context
        aidlServiceManager = AidlServiceManager(requireContext())

        myRecyclerUser = binding.recyclerViewUser
        adapterUserList = UsersAdapter(arrayListOf())
        myRecyclerUser.adapter = adapterUserList
        myRecyclerUser.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        myRecyclerUser.setHasFixedSize(true)

        // Define o listener de clique para os itens da RecyclerView.
        adapterUserList.onItemClick = { user ->
            // Define o usuário atual no ViewModel
            viewModel.setCurrentUser(user)
            // Navega para a UserPage. A UserPage deve observar o ViewModel para pegar o usuário selecionado.
            // Não é necessário passar o UserModel via Bundle se a UserPage o pega do ViewModel.
            findNavController().navigate(R.id.action_welcome_to_userPage)
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

                        // Se não há perfis, nenhum usuário deve estar selecionado no ViewModel
                        viewModel.setCurrentUser(null)

                        // Se o audioManager já foi inicializado, pare a música
                        if (::audioManager.isInitialized) {
                            audioManager.stop()
                            Log.d("Welcome", "No profiles found, stopping music.")
                        }
                    } else {
                        binding.txtwelcome.text = getString(R.string.select_profile_header_text)
                        binding.recyclerViewUser.isVisible = true

                        // Se há perfis, mas nenhum está atualmente selecionado no ViewModel (ex: app recém-aberto, ou perfil deletado)
                        // Seleciona o primeiro perfil da lista como o "ativo" para a sessão.
                        if (uiState.user == null) {
                            viewModel.setCurrentUser(newList.firstOrNull())
                            // A música NÃO DEVE tocar automaticamente aqui. Ela só toca se o usuário clicar Play.
                        }
                    }
                }
            }
        }

        return binding.root
    }

    // Vincular ao serviço AIDL quando o fragmento inicia
    override fun onStart() {
        super.onStart()
        aidlServiceManager.bindService(
            onServiceConnected = { service ->
                audioManager = AudioManager(aidlServiceManager) // Inicializa AudioManager após conexão
                // Verifica se há um usuário selecionado no ViewModel.
                // Se não houver (ex: app inicia sem perfis, ou todos os perfis foram deletados),
                // garante que a música esteja parada.
                if (viewModel.uiState.value.user == null) {
                    audioManager.stop()
                    Log.d("Welcome", "No user selected in ViewModel on service connect, stopping music.")
                }
            },
            onServiceDisconnected = {
                Log.w("Welcome", "AIDL Service disconnected from Welcome fragment.")
            }
        )
    }

    // Desvincular do serviço AIDL quando o fragmento para
    override fun onStop() {
        super.onStop()
        aidlServiceManager.unbindService()
    }

    // Sua classe UsersDiffCallback permanece a mesma
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
