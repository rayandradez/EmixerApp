package your_package_name.ui.screens

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.emixerapp.data.local.database.AppDatabase
import com.example.emixerapp.data.model.UserModel
import com.example.emixerapp.data.repository.UsersRepository
import com.example.emixerapp.ui.components.viewModels.MainViewModel
import com.example.emixerapp.ui.components.viewModels.MainViewModelFactory
import com.example.mvvmapp.R
import com.example.mvvmapp.databinding.FragmentUserPageBinding
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.launch


class UserPage : Fragment() {
    private var _binding: FragmentUserPageBinding? = null
    // Define uma propriedade somente leitura para acessar a binding, evitando o uso direto de _binding.
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    // Constante para o código de solicitação de permissão de áudio.
    private val AUDIO_PERMISSION_REQUEST = 100
    private var hasChanges = false  // Indica se há mudanças não salvas.
    private lateinit var analytics: FirebaseAnalytics


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        // Infla o layout usando view binding.
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root // Retorna a raiz do layout inflado.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytics = Firebase.analytics

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

        // Inicializar o banco de dados e o repositório
        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        val usersRepository = UsersRepository(database.usersDao())

        // Inicializar o ViewModel com o Factory compartilhado com a Activity
        val factory = MainViewModelFactory(usersRepository)
        viewModel = ViewModelProvider(requireActivity(), factory)[MainViewModel::class.java]

        // Verifica e solicita as permissões de áudio necessárias com base no nível da API.
        checkAudioPermissions()

        // Define o listener de clique para o botão "Salvar Configurações de Áudio".
        binding.saveAudioSettingsButton.setOnClickListener {
            // Salva as configurações de áudio e navega para a tela inicial.
            hasChanges = false  // Reseta a flag de mudanças.
            saveAudioSettings()
            findNavController().navigate(R.id.action_userPage_to_welcome)   // Navega para a tela de boas-vindas.
        }

        binding.txtUserPageMessage.setOnClickListener {
            // Verifica se há mudanças antes de navegar para a tela anterior.
            if (hasChanges) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()  // Navega para a tela anterior.
            }
        }

        // Define listeners para as seek bars de ajuste de áudio.
        binding.bassSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                hasChanges = true   // Marca que houve mudanças.
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário começa a interagir com o SeekBar.
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário termina a interação com o SeekBar.
            }
        })

        binding.midSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                hasChanges = true   // Marca que houve mudanças.
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário começa a interagir com o SeekBar.
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário começa a interagir com o SeekBar.
            }
        })

        binding.highSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                hasChanges = true   // Marca que houve mudanças.
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário começa a interagir com o SeekBar.
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário termina a interação com o SeekBar.
            }
        })

        binding.mainVolumeSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                hasChanges = true   // Marca que houve mudanças.
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário começa a interagir com o SeekBar.
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário termina a interação com o SeekBar.
            }
        })

        binding.panSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                hasChanges = true   // Marca que houve mudanças.
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário começa a interagir com o SeekBar.
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                // Este método é intencionalmente deixado vazio porque não precisamos
                // realizar nenhuma ação quando o usuário termina a interação com o SeekBar.
            }
        })

        // Define o listener de clique para o botão "Redefinir Configurações de Áudio".
        binding.resetAudioSettingsButton.setOnClickListener {
            // Redefine as configurações de áudio para os valores padrão.
            hasChanges = true // Define hasChanges como true quando o ícone muda.
            resetToDefaults()   // Chama a função que redefine as configurações.
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
                    hasChanges = false // Define hasChanges como false no primeiro carregamento da tela.
                }
            }
        }

    }

    private fun saveAudioSettings() {
        // Atualiza e salva as configurações de áudio do usuário no ViewModel.
        viewModel.updateUser(viewModel.uiState.value.user?.let {
            analytics.logEvent("eqSave", ) {
                param("profileid", it.id)
                param("profilename", it.name)
                param("bass", binding.bassSeekBar.progress.toLong())
                param("mid", binding.midSeekBar.progress.toLong())
                param("high", binding.highSeekBar.progress.toLong())
                param("main", binding.mainVolumeSeekBar.progress.toLong())
                param("pan", binding.panSeekBar.progress.toLong())
            }
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
        hasChanges = false
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
