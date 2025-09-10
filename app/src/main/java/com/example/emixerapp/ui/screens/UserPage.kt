package com.example.emixerapp.ui.screens

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
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
import com.reaj.emixer.IMessageService
import com.reaj.emixer.R
import com.reaj.emixer.data.local.database.AppDatabase
import com.reaj.emixer.data.model.UserModel
import com.reaj.emixer.data.repository.UsersRepository
import com.reaj.emixer.databinding.FragmentUserPageBinding
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.ui.components.viewModels.MainViewModelFactory
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class UserPage : Fragment() {

    // --- Lógica de Permissão (sem alterações) ---
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_CONTACTS)
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { Log.d(TAG, "Permissão ${it.key} concedida: ${it.value}") }
        }
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter { ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED }
        if (permissionsToRequest.isNotEmpty()) { requestPermissionLauncher.launch(permissionsToRequest.toTypedArray()) }
    }

    private var _binding: FragmentUserPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var hasChanges = false

    private lateinit var audioManager: AudioManager
    private lateinit var audioSettingsManager: AudioSettingsManager

    private val playbackHandler = Handler(Looper.getMainLooper())
    private lateinit var updatePlaybackProgressRunnable: Runnable
    private var currentTrackIndex: Int = 0
    private lateinit var lottieAnimationView: LottieAnimationView
    private val trackLottieMap = mapOf(0 to R.raw.skull, 1 to R.raw.dancing)

    private val serviceConnectedCallback: (IMessageService) -> Unit = { service ->
        Log.d(TAG, "Serviço AIDL conectado.")
        audioManager = AudioManager(AidlServiceManager)
        audioSettingsManager = AudioSettingsManager(
            WeakReference(this), service, AidlServiceManager.isServiceBound(),
            onSettingsChanged = { hasChanges = true },
            onBassChanged = { value -> audioManager.setBass(value) },
            onMidChanged = { value -> audioManager.setMid(value) },
            onTrebleChanged = { value -> audioManager.setTreble(value) },
            onMainVolumeChanged = { value -> audioManager.setMainVolume(value) },
            onPanChanged = { value -> audioManager.setPan(value) }
        )

        // Sincroniza com o perfil atual assim que o serviço conecta
        viewModel.uiState.value.user?.let { syncProfile(it) }

        setupPlaybackControls()
        syncUiWithServiceState(service)
        playbackHandler.post(updatePlaybackProgressRunnable)
    }

    private val serviceDisconnectedCallback: () -> Unit = {
        Log.w(TAG, "Serviço AIDL desconectado.")
        playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
        if (view != null) lottieAnimationView.pauseAnimation()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = MainViewModelFactory(UsersRepository(AppDatabase.getDatabase(requireContext()).usersDao()))
        viewModel = ViewModelProvider(requireActivity(), factory)[MainViewModel::class.java]

        lottieAnimationView = binding.animationView
        setupBackPressedDispatcher()
        checkAndRequestPermissions()

        observeUiState()

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

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    val user = data.user ?: return@collect
                    binding.txtUserName.text = getString(R.string.welcome) + ", " + (user.name?.substringBefore(" ") ?: "Guest")
                    if (!hasChanges) {
                        syncProfile(user)
                    }
                }
            }
        }
    }

    private fun syncProfile(user: UserModel) {
        if (_binding == null || !AidlServiceManager.isServiceBound() || !::audioManager.isInitialized) {
            Log.w(TAG, "syncProfile chamado, mas a UI ou o serviço não estão prontos. Tentará novamente.")
            return
        }
        Log.d(TAG, "Sincronizando perfil completo para '${user.name}'.")

        detachSeekBarListeners()

        binding.bassSeekBar.progress = user.bass
        binding.midSeekBar.progress = user.middle
        binding.highSeekBar.progress = user.high
        binding.mainVolumeSeekBar.progress = user.mainVolume
        binding.panSeekBar.progress = (user.pan + 100) / 2
        Log.d(TAG, "UI atualizada para '${user.name}' (Pan Progress: ${binding.panSeekBar.progress})")

        audioManager.setBass(user.bass)
        audioManager.setMid(user.middle)
        audioManager.setTreble(user.high)
        audioManager.setMainVolume(user.mainVolume)
        audioManager.setPan(user.pan) // Envia o valor correto (-100 a 100)
        Log.d(TAG, "Configurações de áudio enviadas para o serviço para '${user.name}' (Pan: ${user.pan})")

        attachSeekBarListeners()
    }

    private fun detachSeekBarListeners() {
        Log.d(TAG, "Desligando listeners dos SeekBars.")
        binding.bassSeekBar.setOnSeekBarChangeListener(null)
        binding.midSeekBar.setOnSeekBarChangeListener(null)
        binding.highSeekBar.setOnSeekBarChangeListener(null)
        binding.mainVolumeSeekBar.setOnSeekBarChangeListener(null)
        binding.panSeekBar.setOnSeekBarChangeListener(null)
    }

    private fun attachSeekBarListeners() {
        Log.d(TAG, "Religando listeners dos SeekBars.")
        if (::audioSettingsManager.isInitialized) {
            audioSettingsManager.setupSeekBarListeners(
                binding.bassSeekBar, binding.midSeekBar, binding.highSeekBar,
                binding.mainVolumeSeekBar, binding.panSeekBar
            )
        }
    }

    // --- Métodos restantes sem alterações ---
    private fun syncUiWithServiceState(service: IMessageService) {
        if (view == null) return; try {
            currentTrackIndex = service.selectedTrackIndex;
            val isPlaying = service.isPlaying;
            val duration = service.duration;
            val position = service.currentPosition;
            updateLottieAnimation(currentTrackIndex, isPlaying);
            binding.btnPlay.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

            if (duration > 0) {
                binding.playbackSeekBar.max = duration;
                binding.playbackSeekBar.progress = position;
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao sincronizar UI: ${e.message}");
        }
    }
    private fun setupPlaybackControls() { binding.btnPlay.setOnClickListener { if (!::audioManager.isInitialized) return@setOnClickListener; if (audioManager.isPlaying()) audioManager.pause() else audioManager.play() }; binding.btnStop.setOnClickListener { if (!::audioManager.isInitialized) return@setOnClickListener; audioManager.stop() }; binding.btnPrevious.setOnClickListener { if (!::audioManager.isInitialized) return@setOnClickListener; val tracks = audioManager.getAvailableTracks(); if (tracks.isNotEmpty()) { val newIndex = (currentTrackIndex - 1 + tracks.size) % tracks.size; audioManager.selectTrack(newIndex) } }; binding.btnNext.setOnClickListener { if (!::audioManager.isInitialized) return@setOnClickListener; val tracks = audioManager.getAvailableTracks(); if (tracks.isNotEmpty()) { val newIndex = (currentTrackIndex + 1) % tracks.size; audioManager.selectTrack(newIndex) } }; binding.playbackSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}; override fun onStartTrackingTouch(seekBar: SeekBar?) { playbackHandler.removeCallbacks(updatePlaybackProgressRunnable) }; override fun onStopTrackingTouch(seekBar: SeekBar?) { if (::audioManager.isInitialized) { seekBar?.progress?.let { audioManager.seekTo(it) }; playbackHandler.post(updatePlaybackProgressRunnable) } } }) }
    private fun saveAudioSettingsAndNavigate() { saveAudioSettings(); findNavController().navigate(R.id.action_userPage_to_welcome) }
    private fun saveAudioSettings() { hasChanges = false; val user = viewModel.uiState.value.user ?: return; val panValue = (binding.panSeekBar.progress * 2) - 100; val updatedUser = user.copy(bass = binding.bassSeekBar.progress, middle = binding.midSeekBar.progress, high = binding.highSeekBar.progress, mainVolume = binding.mainVolumeSeekBar.progress, pan = panValue); viewModel.updateUser(updatedUser); Toast.makeText(requireContext(), "Audio settings saved", Toast.LENGTH_SHORT).show() }
    private fun resetAudioSettings() { if (!::audioSettingsManager.isInitialized) return; hasChanges = true; audioSettingsManager.resetToDefaults(binding.bassSeekBar, binding.midSeekBar, binding.highSeekBar, binding.mainVolumeSeekBar, binding.panSeekBar) }
    private fun showDiscardChangesDialog() { AlertDialog.Builder(requireContext()).setTitle("Discard Changes?").setMessage("You have unsaved changes. Do you want to discard them?").setPositiveButton("Discard") { _, _ -> findNavController().navigateUp() }.setNegativeButton("Cancel", null).show() }
    private fun setupBackPressedDispatcher() { requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() { if (hasChanges) showDiscardChangesDialog() else findNavController().navigateUp() } }) }
    private fun showTrackSelectionDialog() { if (!::audioManager.isInitialized) return; val availableTracks = audioManager.getAvailableTracks().toTypedArray<CharSequence>(); if (availableTracks.isEmpty()) return; AlertDialog.Builder(requireContext()).setTitle("Select a Track").setItems(availableTracks) { _, which -> audioManager.selectTrack(which) }.setNegativeButton("Cancel", null).show() }
    private fun updateLottieAnimation(trackIndex: Int, isPlaying: Boolean) { if (view == null) return; val lottieResId = trackLottieMap[trackIndex] ?: R.raw.skull; if (lottieAnimationView.tag != lottieResId) { lottieAnimationView.setAnimation(lottieResId); lottieAnimationView.tag = lottieResId; Log.d(TAG, "Nova animação Lottie carregada: $lottieResId") }; if (isPlaying && !lottieAnimationView.isAnimating) { lottieAnimationView.playAnimation() } else if (!isPlaying && lottieAnimationView.isAnimating) { lottieAnimationView.pauseAnimation() } }
    companion object { private const val TAG = "UserPage" }
}
