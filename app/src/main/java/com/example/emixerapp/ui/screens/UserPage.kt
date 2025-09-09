package com.example.emixerapp.ui.screens

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.emixerapp.manager.AidlServiceManager
import com.example.emixerapp.manager.AudioManager
import com.example.emixerapp.manager.AudioSettingsManager
// import com.example.emixerapp.manager.PermissionManager // <<< PODE REMOVER ESTE IMPORT
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.reaj.emixer.IMessageService
import com.reaj.emixer.R
import com.reaj.emixer.data.local.database.AppDatabase
import com.reaj.emixer.data.repository.UsersRepository
import com.reaj.emixer.databinding.FragmentUserPageBinding
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.ui.components.viewModels.MainViewModelFactory
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class UserPage : Fragment() {

    // --- LÓGICA DE PERMISSÃO INTEGRADA DIRETAMENTE AQUI ---

    // 1. Define as permissões necessárias com base na versão do Android.
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_CONTACTS
        )
    }

    // 2. Registra o "lançador" de permissões.
    //    Isto é feito na inicialização da classe, o que acontece cedo o suficiente no ciclo de vida.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Log.d(TAG, "Permissão concedida: $permissionName")
                } else {
                    Log.w(TAG, "Permissão negada: $permissionName")
                    // Opcional: Mostrar um aviso se uma permissão crucial for negada.
                }
            }
        }

    // 3. Função para verificar e solicitar as permissões.
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    // --- FIM DA LÓGICA DE PERMISSÃO ---


    private var _binding: FragmentUserPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var analytics: FirebaseAnalytics
    private var hasChanges = false

    // Não precisamos mais da variável permissionManager
    // private lateinit var permissionManager: PermissionManager
    private lateinit var audioManager: AudioManager
    private lateinit var audioSettingsManager: AudioSettingsManager

    private val playbackHandler = Handler(Looper.getMainLooper())
    private lateinit var updatePlaybackProgressRunnable: Runnable
    private var currentTrackIndex: Int = 0

    private lateinit var lottieAnimationView: LottieAnimationView
    private val trackLottieMap = mapOf(
        0 to R.raw.skull,
        1 to R.raw.dancing
    )

    private val serviceConnectedCallback: (IMessageService) -> Unit = { service ->
        Log.d(TAG, "Serviço AIDL conectado (callback UserPage). Inicializando managers.")
        audioManager = AudioManager(AidlServiceManager)
        audioSettingsManager = AudioSettingsManager(
            WeakReference(this),
            service,
            AidlServiceManager.isServiceBound(),
            onSettingsChanged = { hasChanges = true },
            onBassChanged = { value -> audioManager.setBass(value) },
            onMidChanged = { value -> audioManager.setMid(value) },
            onTrebleChanged = { value -> audioManager.setTreble(value) },
            onMainVolumeChanged = { value -> audioManager.setMainVolume(value) },
            onPanChanged = { value -> audioManager.setPan(value) }
        )

        audioSettingsManager.setupSeekBarListeners(
            binding.bassSeekBar, binding.midSeekBar, binding.highSeekBar,
            binding.mainVolumeSeekBar, binding.panSeekBar
        )

        setupPlaybackControls()
        observeUiState()
        syncUiWithServiceState(service)
        playbackHandler.post(updatePlaybackProgressRunnable)
    }

    private val serviceDisconnectedCallback: () -> Unit = {
        Log.w(TAG, "Serviço AIDL desconectado (callback UserPage).")
        playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
        if (view != null) lottieAnimationView.pauseAnimation()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analytics = Firebase.analytics

        val factory = MainViewModelFactory(UsersRepository(AppDatabase.getDatabase(requireContext()).usersDao()))
        viewModel = ViewModelProvider(requireActivity(), factory)[MainViewModel::class.java]

        lottieAnimationView = binding.animationView
        setupBackPressedDispatcher()

        // Chama a nova função local para verificar permissões
        checkAndRequestPermissions()

        updatePlaybackProgressRunnable = object : Runnable {
            override fun run() {
                if (AidlServiceManager.isServiceBound() && view != null) {
                    syncUiWithServiceState(AidlServiceManager.messageService!!)
                }
                playbackHandler.postDelayed(this, 1000)
            }
        }

        binding.saveAudioSettingsButton.setOnClickListener { saveAudioSettingsAndNavigate() }
        binding.resetAudioSettingsButton.setOnClickListener { resetAudioSettings() }
        binding.btnSelectTrack.setOnClickListener { showTrackSelectionDialog() }
    }

    override fun onStart() {
        super.onStart()
        AidlServiceManager.addServiceConnectedCallback(serviceConnectedCallback)
        AidlServiceManager.addServiceDisconnectedCallback(serviceDisconnectedCallback)
    }

    override fun onStop() {
        super.onStop()
        AidlServiceManager.removeServiceConnectedCallback(serviceConnectedCallback)
        AidlServiceManager.removeServiceDisconnectedCallback(serviceDisconnectedCallback)
        playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // O resto do arquivo permanece igual...

    private fun syncUiWithServiceState(service: IMessageService) {
        if (view == null) return
        try {
            currentTrackIndex = service.selectedTrackIndex
            val isPlaying = service.isPlaying
            val duration = service.duration
            val position = service.currentPosition
            updateLottieAnimation(currentTrackIndex, isPlaying)
            if (duration > 0) {
                binding.playbackSeekBar.max = duration
                binding.playbackSeekBar.progress = position
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao sincronizar UI: ${e.message}")
        }
    }

    private fun setupPlaybackControls() {
        binding.btnPlay.setOnClickListener {
            if (!::audioManager.isInitialized) return@setOnClickListener
            if (audioManager.isPlaying()) {
                audioManager.pause()
            } else {
                audioManager.play()
            }
        }

        binding.btnStop.setOnClickListener {
            if (!::audioManager.isInitialized) return@setOnClickListener
            audioManager.stop()
        }

        binding.btnPrevious.setOnClickListener {
            if (!::audioManager.isInitialized) return@setOnClickListener
            val tracks = audioManager.getAvailableTracks()
            if (tracks.isNotEmpty()) {
                val newIndex = (currentTrackIndex - 1 + tracks.size) % tracks.size
                audioManager.selectTrack(newIndex)
            }
        }

        binding.btnNext.setOnClickListener {
            if (!::audioManager.isInitialized) return@setOnClickListener
            val tracks = audioManager.getAvailableTracks()
            if (tracks.isNotEmpty()) {
                val newIndex = (currentTrackIndex + 1) % tracks.size
                audioManager.selectTrack(newIndex)
            }
        }

        binding.playbackSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (::audioManager.isInitialized) {
                    seekBar?.progress?.let { audioManager.seekTo(it) }
                    playbackHandler.post(updatePlaybackProgressRunnable)
                }
            }
        })
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    val userName = data.user?.name?.substringBefore(" ") ?: "Guest"
                    val welcomeText = getString(R.string.welcome) + ", "
                    val fullText = welcomeText + userName

                    val spannableString = SpannableStringBuilder(fullText)
                    spannableString.setSpan(StyleSpan(Typeface.BOLD), welcomeText.length, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    binding.txtUserName.text = spannableString

                    if (!hasChanges) {
                        binding.bassSeekBar.progress = data.user?.bass ?: 50
                        binding.midSeekBar.progress = data.user?.middle ?: 50
                        binding.highSeekBar.progress = data.user?.high ?: 50
                        binding.mainVolumeSeekBar.progress = data.user?.mainVolume ?: 50
                        binding.panSeekBar.progress = (data.user?.pan?.let { (it + 100) / 2 } ?: 50)
                    }
                }
            }
        }
    }

    private fun saveAudioSettingsAndNavigate() {
        saveAudioSettings()
        findNavController().navigate(R.id.action_userPage_to_welcome)
    }

    private fun saveAudioSettings() {
        hasChanges = false
        val user = viewModel.uiState.value.user ?: return
        val panValue = (binding.panSeekBar.progress * 2) - 100
        val updatedUser = user.copy(
            bass = binding.bassSeekBar.progress,
            middle = binding.midSeekBar.progress,
            high = binding.highSeekBar.progress,
            mainVolume = binding.mainVolumeSeekBar.progress,
            pan = panValue
        )
        viewModel.updateUser(updatedUser)
        Toast.makeText(requireContext(), "Audio settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun resetAudioSettings() {
        if (!::audioSettingsManager.isInitialized) return
        hasChanges = true
        audioSettingsManager.resetToDefaults(binding.bassSeekBar, binding.midSeekBar, binding.highSeekBar, binding.mainVolumeSeekBar, binding.panSeekBar)
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Do you want to discard them?")
            .setPositiveButton("Discard") { _, _ -> findNavController().navigateUp() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasChanges) showDiscardChangesDialog() else findNavController().navigateUp()
            }
        })
    }

    private fun showTrackSelectionDialog() {
        if (!::audioManager.isInitialized) return
        val availableTracks = audioManager.getAvailableTracks().toTypedArray<CharSequence>()
        if (availableTracks.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Select a Track")
            .setItems(availableTracks) { _, which -> audioManager.selectTrack(which) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateLottieAnimation(trackIndex: Int, isPlaying: Boolean) {
        if (view == null) return

        val lottieResId = trackLottieMap[trackIndex] ?: R.raw.skull

        if (lottieAnimationView.tag != lottieResId) {
            lottieAnimationView.setAnimation(lottieResId)
            lottieAnimationView.tag = lottieResId
            Log.d(TAG, "Nova animação Lottie carregada: $lottieResId")
        }

        if (isPlaying && !lottieAnimationView.isAnimating) {
            lottieAnimationView.playAnimation()
        } else if (!isPlaying && lottieAnimationView.isAnimating) {
            lottieAnimationView.pauseAnimation()
        }
    }

    companion object {
        private const val TAG = "UserPage"
    }
}
