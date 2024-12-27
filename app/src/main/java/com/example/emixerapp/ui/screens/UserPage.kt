package your_package_name.ui.screens

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.FragmentUserPageBinding
import kotlinx.coroutines.launch


class UserPage : Fragment() {
    private var _binding: FragmentUserPageBinding? = null
    // Define uma propriedade somente leitura para acessar a binding, evitando o uso direto de _binding.
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    // Constante para o código de solicitação de permissão de áudio.
    private val AUDIO_PERMISSION_REQUEST = 100
    private var hasChanges = false  // Indica se há mudanças não salvas.



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla o layout usando view binding.
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
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

        viewModel =
                // Obtém o ViewModel para gerenciar o estado da UI.
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // Obtém o ViewModel




        // Verifica e solicita as permissões de áudio necessárias com base no nível da API.
        checkAudioPermissions()

        // Define o listener de clique para o botão "Salvar Configurações de Áudio".
        binding.saveAudioSettingsButton.setOnClickListener {
            // Salva as configurações de áudio e navega para a tela inicial.
            saveAudioSettings()
            findNavController().navigate(R.id.action_userPage_to_welcome)
        }

        binding.txtUserPageMessage.setOnClickListener {
            if (hasChanges) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()  // Navega para a tela anterior.
            }
        }

        // Define o listener de clique para o botão "Redefinir Configurações de Áudio".
        binding.resetAudioSettingsButton.setOnClickListener {
            // Redefine as configurações de áudio para os valores padrão.
            hasChanges = true // Define hasChanges como true quando o ícone muda.
            resetToDefaults()
        }

        // Observa o estado da UI e atualiza os componentes da UI.
        viewLifecycleOwner.lifecycleScope.launch {
            // Repete este bloco de código sempre que o ciclo de vida estiver no estado STARTED.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Coleta dados do fluxo de estado da UI do ViewModel.
                viewModel.uiState.collect { data ->
                    // Atualiza os componentes da UI com os dados do usuário.
                    binding.txtUserName.text = data.user?.name?.substringBefore(" ")
                    binding.bassSeekBar.progress = data.user?.bass!!
                    binding.midSeekBar.progress = data.user!!.middle
                    binding.highSeekBar.progress = data.user!!.high
                    binding.mainVolumeSeekBar.progress = data.user!!.mainVolume
                    binding.panSeekBar.progress = data.user!!.pan
                    hasChanges = true // Define hasChanges como true quando o ícone muda.
                }
            }
        }

    }

    private fun saveAudioSettings() {
        // Atualiza e salva as configurações de áudio do usuário no ViewModel.
        viewModel.updateUser(viewModel.uiState.value.user?.let {
            UserModel(
                it.id,
                it.name,
                it.iconIndex,
                binding.bassSeekBar.progress,
                binding.midSeekBar.progress,
                binding.highSeekBar.progress,
                binding.mainVolumeSeekBar.progress,
                binding.panSeekBar.progress
            )
        })
        // Informa ao usuário que as configurações foram salvas.
        Toast.makeText(requireContext(), "Audio settings saved (simulated)", Toast.LENGTH_SHORT)
            .show()
    }

    private fun resetToDefaults() {
        // Redefine todas as configurações de áudio para os valores padrão.
        binding.bassSeekBar.progress = 0
        binding.midSeekBar.progress = 0
        binding.highSeekBar.progress = 0
        binding.mainVolumeSeekBar.progress = 50
        binding.panSeekBar.progress = 50
    }

    private fun checkAudioPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para API nível 33 e superior, use READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Solicita a permissão READ_MEDIA_AUDIO
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO
                    ),
                    AUDIO_PERMISSION_REQUEST
                )
            }
        } else {
            // Para níveis de API abaixo de 33, considere usar permissões mais antigas, como RECORD_AUDIO, se aplicável.
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Solicita a permissão RECORD_AUDIO
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO
                    ),
                    AUDIO_PERMISSION_REQUEST
                )
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpa a referência de vinculação para evitar vazamentos de memória
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
