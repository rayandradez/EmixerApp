// UserPage.kt
package com.example.emixerapp.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.emixerapp.manager.AidlServiceManager
import com.example.emixerapp.manager.AudioManager
import com.example.emixerapp.manager.AudioSettingsManager
import com.example.emixerapp.manager.PermissionManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.reaj.emixer.R
import com.reaj.emixer.data.local.database.AppDatabase
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.data.repository.UsersRepository
import com.reaj.emixer.databinding.FragmentUserPageBinding
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.ui.components.viewModels.MainViewModelFactory
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Fragmento responsável por exibir e permitir a edição das configurações de áudio do usuário.
 */
class UserPage : Fragment() {

    private var _binding: FragmentUserPageBinding? = null
    // Garante que a binding só seja acessada quando não for nula
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var analytics: FirebaseAnalytics

    private val AUDIO_PERMISSION_REQUEST = 100 // Código para a requisição de permissão de áudio
    private var hasChanges = false // Flag para indicar se houve alterações não salvas

    private lateinit var permissionManager: PermissionManager
    private lateinit var aidlServiceManager: AidlServiceManager
    private lateinit var audioManager: AudioManager
    private lateinit var audioSettingsManager: AudioSettingsManager



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Infla o layout do fragmento usando ViewBinding
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytics = Firebase.analytics // Inicializa o Firebase Analytics

        // Inicializa os Managers
        aidlServiceManager = AidlServiceManager(requireContext())
        permissionManager = PermissionManager(requireContext(), requireActivity())

        // Inicializa o ViewModel
        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        val usersRepository = UsersRepository(database.usersDao())
        val factory = MainViewModelFactory(usersRepository)
        viewModel = ViewModelProvider(requireActivity(), factory)[MainViewModel::class.java]

        // Configura o tratamento do botão de voltar
        setupBackPressedDispatcher()

        // Verifica e solicita as permissões de áudio
        checkAudioPermissions()

        // Vincula ao serviço AIDL e configura as definições de áudio
        bindAidlService()

        // Configura os listeners de clique
        binding.saveAudioSettingsButton.setOnClickListener { saveAudioSettingsAndNavigate() }
        binding.txtUserPageMessage.setOnClickListener { navigateBack() }
        binding.resetAudioSettingsButton.setOnClickListener { resetAudioSettings() }
    }

    /**
     * Vincula ao serviço AIDL e configura as definições de áudio
     */
    private fun bindAidlService() {
        aidlServiceManager.bindService(
            onServiceConnected = { service ->
                // Inicializa o AudioManager
                audioManager = AudioManager(aidlServiceManager)
                // Inicializa o AudioSettingsManager quando o serviço é conectado
                audioSettingsManager = AudioSettingsManager(
                    WeakReference(this), // Referência fraca para evitar vazamento de memória
                    service, // Interface AIDL para comunicação
                    aidlServiceManager.isServiceBound(), // Indica se o serviço está vinculado
                    onSettingsChanged = { hasChanges = true }, // Callback para quando as configurações mudam
                    onBassChanged = { value -> audioManager.setBass(value) }, // Callback para mudança no Bass
                    onMidChanged = { value -> audioManager.setMid(value) }, // Callback para mudança no Mid
                    onTrebleChanged = { value -> audioManager.setTreble(value) }, // Callback para mudança no Treble
                    onMainVolumeChanged = { value -> audioManager.setMainVolume(value) }, // Callback para mudança no Volume Principal
                    onPanChanged = { value -> audioManager.setPan(value) },  // Callback para mudança no Pan
                    playAudio = {  -> audioManager.playAudio() }, // Callback para tocar audio
                    pauseAudio = {  -> audioManager.stopAudio() }, // Callback para parar audio
                    stopAudio = {  -> audioManager.pauseAudio() }  // Callback para pausar audio
                )

                // Configura os listeners para as SeekBars
                audioSettingsManager.setupSeekBarListeners(
                    binding.bassSeekBar,
                    binding.midSeekBar,
                    binding.highSeekBar,
                    binding.mainVolumeSeekBar,
                    binding.panSeekBar,
                    binding.btnPlay,
                    binding.btnPause,
                    binding.btnStop,
                )

                // Observa o estado da UI e atualiza os componentes
                observeUiState()
            },
            onServiceDisconnected = {
                Log.w("UserPage", "AIDL Service disconnected") // Log de desconexão
            }
        )
    }

    /**
     * Configura o tratamento do botão de voltar para confirmar o descarte de alterações.
     */
    private fun setupBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasChanges) {
                    showDiscardChangesDialog() // Mostra o diálogo de confirmação
                } else {
                    findNavController().navigateUp() // Navega para trás
                }
            }
        })
    }

    /**
     * Verifica e solicita as permissões de áudio necessárias.
     */
    private fun checkAudioPermissions() {
        permissionManager.checkAudioPermissions(AUDIO_PERMISSION_REQUEST)
    }

    /**
     * Observa o estado da UI (UiState) do ViewModel e atualiza os componentes da tela.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    // Atualiza os elementos da UI com os dados do usuário
                    binding.txtUserName.text = data.user?.name?.substringBefore(" ")
                    binding.bassSeekBar.progress = data.user?.bass ?: 0 // Define o progresso do Bass
                    binding.midSeekBar.progress = data.user?.middle ?: 0 // Define o progresso do Mid
                    binding.highSeekBar.progress = data.user?.high ?: 0 // Define o progresso do Treble
                    binding.mainVolumeSeekBar.progress = data.user?.mainVolume ?: 50 // Define o progresso do Volume Principal
                    binding.panSeekBar.progress = data.user?.pan ?: 50 // Define o progresso do Pan
                    hasChanges = false // Reseta a flag de alterações
                }
            }
        }
    }

    /**
     * Salva as configurações de áudio e navega para a tela de boas-vindas.
     */
    private fun saveAudioSettingsAndNavigate() {
        hasChanges = false // Reseta a flag de alterações
        saveAudioSettings() // Salva as configurações
        findNavController().navigate(R.id.action_userPage_to_welcome) // Navega para a tela de boas-vindas
    }

    /**
     * Navega para a tela anterior, mostrando um diálogo de confirmação se houver alterações não salvas.
     */
    private fun navigateBack() {
        if (hasChanges) {
            showDiscardChangesDialog() // Mostra o diálogo de confirmação
        } else {
            findNavController().navigateUp() // Navega para trás
        }
    }

    /**
     * Salva as configurações de áudio do usuário.
     */
    private fun saveAudioSettings() {
        viewModel.updateUser(viewModel.uiState.value.user?.let {
            // Registra um evento no Firebase Analytics
            analytics.logEvent("eqSave") {
                param("profileid", it.id) // ID do perfil
                param("profilename", it.name) // Nome do perfil
                param("bass", binding.bassSeekBar.progress.toLong()) // Valor do Bass
                param("mid", binding.midSeekBar.progress.toLong()) // Valor do Mid
                param("high", binding.highSeekBar.progress.toLong()) // Valor do Treble
                param("main", binding.mainVolumeSeekBar.progress.toLong()) // Valor do Volume Principal
                param("pan", binding.panSeekBar.progress.toLong()) // Valor do Pan
            }
            // Cria um novo UserModel com as configurações atuais
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

        hasChanges = false // Reseta a flag de alterações
        Toast.makeText(requireContext(), "Audio settings saved (simulated)", Toast.LENGTH_SHORT).show() // Exibe uma mensagem
    }

    /**
     * Redefine as configurações de áudio para os valores padrão.
     */
    private fun resetAudioSettings() {
        hasChanges = true // Define a flag de alterações como true
        audioSettingsManager.resetToDefaults(
            binding.bassSeekBar,
            binding.midSeekBar,
            binding.highSeekBar,
            binding.mainVolumeSeekBar,
            binding.panSeekBar
        )
    }

    /**
     * Exibe um diálogo de confirmação para descartar as alterações não salvas.
     */
    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard Changes?") // Título do diálogo
            .setMessage("You have unsaved changes. Do you want to discard them?") // Mensagem do diálogo
            .setPositiveButton("Discard") { _, _ -> findNavController().navigateUp() } // Ação para descartar
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() } // Ação para cancelar
            .show() // Exibe o diálogo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpa a referência da binding para evitar vazamentos de memória
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        aidlServiceManager.unbindService() // Desvincula do serviço AIDL
    }
}
