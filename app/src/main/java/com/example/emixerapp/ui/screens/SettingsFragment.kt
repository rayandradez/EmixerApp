// SettingsFragment.kt
package com.example.emixerapp.ui.screens

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.emixerapp.manager.AidlServiceManager
import com.reaj.emixer.Profile
import com.reaj.emixer.R
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.databinding.FragmentSettingsBinding

/**
 * Fragmento para exibir as configurações do aplicativo.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null // Variável para armazenar a instância do ViewBinding
    private val binding get() = _binding!! // Obtém a instância do ViewBinding, garantindo que não seja nula
    private lateinit var viewModel: MainViewModel // ViewModel para gerenciar os dados da UI
    private lateinit var aidlServiceManager: AidlServiceManager // Gerenciador para a comunicação com o serviço AIDL

    // Registra um launcher para solicitar permissão de leitura de contatos.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                importContacts() // Se a permissão for concedida, importa os contatos
            } else {
                Toast.makeText(
                    context,
                    "Unable to import your profile",
                    Toast.LENGTH_LONG
                ).show() // Se a permissão for negada, exibe uma mensagem
            }
        }

    /**
     * Infla o layout do fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)  // Infla o layout usando ViewBinding
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]  // Obtém uma instância do ViewModel
        aidlServiceManager = AidlServiceManager(requireContext()) // Initialize AidlServiceManager
        return binding.root  // Retorna a view raiz do layout
    }


    /**
     * Configura a UI e define os listeners dos botões.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Define o listener para o botão de importar contatos
        binding.BtnImportContacts.setOnClickListener {
            handleImportContactsClick() // Lida com o clique no botão de importar contatos
        }

        // Define o listener para o botão de gerenciar usuários
        binding.BtnManageUser.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_to_manageUser)  // Navega para a tela de gerenciamento de usuários
        }

        // Define o listener para o botão de visão geral do sistema
        binding.btnTestTasks.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_infoFragment) // Navega para a tela de visão geral do sistema
        }

        // Define o listener para o botão de teste do serviço AIDL
        binding.BtnServiceAIDL.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_ServiceAIDLTest)  // Navega para a tela de teste do serviço AIDL

        }

    }

    /**
     * Limpa a referência do ViewBinding e desvincula do serviço AIDL para evitar vazamentos de memória.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        unbindAidlService() // Unbind from the service
        _binding = null
    }


    /**
     * Desvincula do serviço AIDL.
     */
    private fun unbindAidlService() {
        aidlServiceManager.unbindService() // Desvincula do serviço AIDL
    }



    /**
     * Lida com o clique no botão de importar contatos.
     */
    private fun handleImportContactsClick() {
        // Verifica se a permissão de leitura de contatos já foi concedida
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissão já concedida; nenhuma ação necessária
            importContacts()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            // Aqui você pode mostrar uma explicação ao usuário sobre por que a permissão é necessária
            Toast.makeText(
                requireContext(),
                "Contacts permission is required to import contacts.",
                Toast.LENGTH_SHORT
            ).show()
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) // Solicita a permissão ao usuário
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) // Solicita a permissão ao usuário
        }
    }

    /**
     * Importa os contatos do usuário.
     */

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
        // Navigate to the home screen
        findNavController().navigate(R.id.welcome)

        // Show a toast message
        Toast.makeText(context, "Contacts Added", Toast.LENGTH_SHORT).show()
    }
}
