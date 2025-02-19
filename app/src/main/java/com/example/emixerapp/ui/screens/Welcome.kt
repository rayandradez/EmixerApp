package com.reaj.emixer.ui.screens

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.ui.components.adapters.UsersAdapter
import com.reaj.emixer.R
import com.reaj.emixer.databinding.FragmentWelcomeBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.reaj.emixer.Profile
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import kotlinx.coroutines.launch

class Welcome : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var viewModel: MainViewModel

    // Referências para a RecyclerView e o adaptador.  Declaradas aqui para melhor organização.
    lateinit var myRecyclerUser: RecyclerView
    lateinit var adapterUserList: UsersAdapter

    // Registra um launcher para solicitar permissão de leitura de contatos.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                importContacts()
            } else {
                // Exibe um Toast informando que a importação do perfil falhou
                Toast.makeText(
                    context,
                    "Unable to import your profile",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

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

        // Define o listener para o botão "Gerenciar Usuário".
        binding.BtnManageUser.setOnClickListener {
            // Navega para a tela de gerenciamento de usuários.
            it.findNavController().navigate(R.id.action_welcome_to_manageUser)
        }

        // Define o listener para o botão de "Importar Contatos"
        binding.BtnImportContacts.setOnClickListener {
            handleImportContactsClick()
        }

        // Define o listener de clique para os itens da RecyclerView.
        adapterUserList.onItemClick = { user ->
            // Define o usuário atual no ViewModel e navega para a tela de detalhes do usuário.
            viewModel.setCurrentUser(user)
            findNavController().navigate(WelcomeDirections.actionWelcomeToUserPage(UserModel()))
        }

        // Coleta as mudanças no estado da UI do ViewModel e atualiza a RecyclerView usando DiffUtil para melhor performance.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    val newList = uiState.usersList
                    val diffResult = DiffUtil.calculateDiff(UsersDiffCallback(adapterUserList.dataSet, newList))
                    adapterUserList.dataSet.clear() // Limpa os dados existentes
                    adapterUserList.dataSet.addAll(newList) // Adiciona os novos dados
                    diffResult.dispatchUpdatesTo(adapterUserList) // Atualiza a RecyclerView eficientemente

                    // Update UI based on profiles availability
                    if (newList.isEmpty()) {
                        binding.txtwelcome.text = getString(R.string.no_profiles_message)
                        binding.BtnManageUser.text = getString(R.string.add_profile_button_text_)
                    } else {
                        binding.txtwelcome.text = getString(R.string.select_or_manage_profiles)
                        binding.BtnManageUser.text = getString(R.string.manage_users_button_text)
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

    private fun handleImportContactsClick() {
        when {
            // Verifica o estado da permissão de leitura de contatos
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida; nenhuma ação necessária
                importContacts()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                // Aqui você pode mostrar uma explicação ao usuário sobre por que a permissão é necessária
                Toast.makeText(
                    requireContext(),
                    "Contacts permission is required to import contacts.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                // Solicita a permissão ao usuário
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun importContacts() {
        // Define as colunas a serem consultadas no banco de dados de contatos
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
        )
        // Consulta os contatos do usuário
        requireContext().contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            // Obtém os índices das colunas relevantes
            val idContactColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameContactColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val profile = mutableListOf<Profile>() // Lista para armazenar perfis de contatos

            while (cursor.moveToNext()) {
                // Extrai os dados do contato
                val id = cursor.getLong(idContactColumn)
                val name = cursor.getString(nameContactColumn)
                val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)

                profile.add(Profile(id, name, uri)) // Adiciona o perfil à lista

                // Cria um UserModel e atualiza no ViewModel

                val user = UserModel(name = name, iconIndex = 0)
                viewModel.updateUser(user)
            }
        }
    }

}