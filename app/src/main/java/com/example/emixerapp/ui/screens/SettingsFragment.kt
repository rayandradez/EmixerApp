// SettingsFragment.kt
package com.example.emixerapp.ui.screens

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
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

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var aidlServiceManager: AidlServiceManager

    // Registra um launcher para solicitar permissão de leitura de contatos.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                importContacts()
            } else {
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
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        aidlServiceManager = AidlServiceManager(requireContext()) // Initialize AidlServiceManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.BtnImportContacts.setOnClickListener {
            handleImportContactsClick()
        }

        binding.BtnSendMessage.setOnClickListener {
            sendAIDLMessage()
        }

        binding.BtnManageUser.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_to_manageUser)
        }
        binding.btnTestTasks.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_infoFragment)
        }

        binding.buttonUpdateValue.setOnClickListener {
            updateValue()
        }

        bindAidlService() // Bind to the service
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindAidlService() // Unbind from the service
        _binding = null
    }


    private fun bindAidlService() {
        aidlServiceManager.bindService(
            onServiceConnected = { aidlInterface ->
                Log.d("SettingsFragment", "Serviço AIDL conectado")
                // Agora você pode usar a interface AIDL
                updateValueOnScreen(aidlInterface.getValue())
            },
            onServiceDisconnected = {
                Log.d("SettingsFragment", "Serviço AIDL desconectado")
                binding.textViewValue.text = "Serviço Desconectado"
            }
        )
    }

    private fun unbindAidlService() {
        aidlServiceManager.unbindService()
    }

    private fun updateValue() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.getMessageService()
                if (aidlInterface != null) {
                    val newValue = (0..100).random() // Gera um valor aleatório
                    aidlInterface.setValue(newValue) // Define o novo valor no serviço
                    updateValueOnScreen(newValue) // Atualiza a tela
                } else {
                    Log.w("SettingsFragment", "Interface AIDL nula")
                    binding.textViewValue.text = "Interface AIDL nula"
                }
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Erro ao chamar o serviço: ${e.message}")
                binding.textViewValue.text = "Erro: ${e.message}"
            }
        } else {
            Log.w("SettingsFragment", "Serviço não vinculado")
            binding.textViewValue.text = "Serviço não vinculado"
        }
    }

    private fun updateValueOnScreen(value: Int) {
        binding.textViewValue.text = "Valor do Serviço: $value"
    }

    private fun sendAIDLMessage() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.getMessageService()
                if (aidlInterface != null) {
                    aidlInterface.sendMessage("Hello from EMIXER AIDL!")
                } else {
                    Log.w("SettingsFragment", "Interface AIDL nula")
                    binding.textViewValue.text = "Interface AIDL nula"
                }
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Erro ao chamar o serviço: ${e.message}")
                binding.textViewValue.text = "Erro: ${e.message}"
            }
        } else {
            Toast.makeText(context, "Service not bound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImportContactsClick() {
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
            // Optionally, you can launch the requestPermissionLauncher here as well, if you want to immediately request the permission after showing the rationale.
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            // Solicita a permissão ao usuário
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
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
        // Navigate to the home screen
        findNavController().navigate(R.id.welcome)

        // Show a toast message
        Toast.makeText(context, "Contacts Added", Toast.LENGTH_SHORT).show()
    }
}
