package com.example.emixerapp.ui.screens

import android.app.AlertDialog
import android.graphics.Typeface
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton // Importe ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

class UserPage : Fragment() {

    private var _binding: FragmentUserPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var analytics: FirebaseAnalytics

    private val AUDIO_PERMISSION_REQUEST = 100
    private var hasChanges = false

    private lateinit var permissionManager: PermissionManager
    private lateinit var aidlServiceManager: AidlServiceManager
    private lateinit var audioManager: AudioManager
    private lateinit var audioSettingsManager: AudioSettingsManager

    private lateinit var playbackHandler: Handler
    private lateinit var updatePlaybackProgressRunnable: Runnable
    private var currentTrackIndex: Int = 0

    private lateinit var lottieAnimationView: LottieAnimationView

    private val trackLottieMap = mapOf(
        0 to R.raw.skull,
        1 to R.raw.dancing
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytics = Firebase.analytics

        aidlServiceManager = AidlServiceManager(requireContext())
        permissionManager = PermissionManager(requireContext(), requireActivity())

        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        val usersRepository = UsersRepository(database.usersDao())
        val factory = MainViewModelFactory(usersRepository)
        viewModel = ViewModelProvider(requireActivity(), factory)[MainViewModel::class.java]

        setupBackPressedDispatcher()
        checkAudioPermissions()

        playbackHandler = Handler(Looper.getMainLooper())
        updatePlaybackProgressRunnable = object : Runnable {
            override fun run() {
                if (aidlServiceManager.isServiceBound()) {
                    val isPlayingAudio = audioManager.isPlaying()
                    if (isPlayingAudio && !lottieAnimationView.isAnimating) {
                        lottieAnimationView.playAnimation()
                    } else if (!isPlayingAudio && lottieAnimationView.isAnimating) {
                        lottieAnimationView.pauseAnimation()
                    }

                    try {
                        val currentPosition = audioManager.getCurrentPosition()
                        val duration = audioManager.getDuration()
                        if (duration > 0) {
                            binding.playbackSeekBar.max = duration
                            binding.playbackSeekBar.progress = currentPosition
                        }
                    } catch (e: RemoteException) {
                        Log.e(TAG, "Error getting playback progress: ${e.message}")
                    }
                }
                playbackHandler.postDelayed(this, 1000)
            }
        }

        lottieAnimationView = binding.animationView

        bindAidlService()

        binding.saveAudioSettingsButton.setOnClickListener { saveAudioSettingsAndNavigate() }
        binding.resetAudioSettingsButton.setOnClickListener { resetAudioSettings() }

        // NOVO: Listener para o botão de seleção de faixa
        binding.btnSelectTrack.setOnClickListener {
            showTrackSelectionDialog()
        }
    }

    private fun bindAidlService() {
        aidlServiceManager.bindService(
            onServiceConnected = { service ->
                audioManager = AudioManager(aidlServiceManager)
                audioSettingsManager = AudioSettingsManager(
                    WeakReference(this),
                    aidlServiceManager.messageService!!,
                    aidlServiceManager.isServiceBound(),
                    onSettingsChanged = { hasChanges = true },
                    onBassChanged = { value -> audioManager.setBass(value) },
                    onMidChanged = { value -> audioManager.setMid(value) },
                    onTrebleChanged = { value -> audioManager.setTreble(value) },
                    onMainVolumeChanged = { value -> audioManager.setMainVolume(value) },
                    onPanChanged = { value -> audioManager.setPan(value) }
                )

                audioSettingsManager.setupSeekBarListeners(
                    binding.bassSeekBar,
                    binding.midSeekBar,
                    binding.highSeekBar,
                    binding.mainVolumeSeekBar,
                    binding.panSeekBar
                )

                setupPlaybackControls()

                observeUiState()

                currentTrackIndex = 0
                updateLottieAnimation(currentTrackIndex, audioManager.isPlaying())

                playbackHandler.post(updatePlaybackProgressRunnable)
            },
            onServiceDisconnected = {
                Log.w(TAG, "AIDL Service disconnected")
                playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
                lottieAnimationView.pauseAnimation()
            }
        )
    }

    private fun setupBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasChanges) {
                    showDiscardChangesDialog()
                } else {
                    findNavController().navigateUp()
                }
            }
        })
    }

    private fun checkAudioPermissions() {
        permissionManager.checkAudioPermissions(AUDIO_PERMISSION_REQUEST)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    val userName = data.user?.name?.substringBefore(" ") ?: "Guest"
                    val welcomeText = getString(R.string.welcome) + ", "
                    val fullText = welcomeText + userName

                    val spannableString = SpannableStringBuilder(fullText)
                    val start = welcomeText.length
                    val end = fullText.length

                    spannableString.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.txtUserName.text = spannableString

                    binding.bassSeekBar.progress = data.user?.bass ?: 50
                    binding.midSeekBar.progress = data.user?.middle ?: 50
                    binding.highSeekBar.progress = data.user?.high ?: 50
                    binding.mainVolumeSeekBar.progress = data.user?.mainVolume ?: 50
                    binding.panSeekBar.progress = (data.user?.pan?.let { (it + 100) / 2 } ?: 50)
                    hasChanges = false
                }
            }
        }
    }

    private fun saveAudioSettingsAndNavigate() {
        hasChanges = false
        saveAudioSettings()
        findNavController().navigate(R.id.action_userPage_to_welcome)
    }

    private fun navigateBack() {
        if (hasChanges) {
            showDiscardChangesDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun saveAudioSettings() {
        viewModel.updateUser(viewModel.uiState.value.user?.let {
            analytics.logEvent("eqSave") {
                param("profileid", it.id)
                param("profilename", it.name)
                param("bass", binding.bassSeekBar.progress.toLong())
                param("mid", binding.midSeekBar.progress.toLong())
                param("high", binding.highSeekBar.progress.toLong())
                param("main", binding.mainVolumeSeekBar.progress.toLong())
                val panValue = (binding.panSeekBar.progress * 2) - 100
                param("pan", panValue.toLong())
            }
            UserModel(
                it.id,
                it.name,
                it.iconIndex,
                binding.bassSeekBar.progress,
                binding.midSeekBar.progress,
                binding.highSeekBar.progress,
                binding.mainVolumeSeekBar.progress,
                (binding.panSeekBar.progress * 2) - 100
            )
        })

        hasChanges = false
        Toast.makeText(requireContext(), "Audio settings saved (simulated)", Toast.LENGTH_SHORT).show()
    }

    private fun resetAudioSettings() {
        hasChanges = true
        audioSettingsManager.resetToDefaults(
            binding.bassSeekBar,
            binding.midSeekBar,
            binding.highSeekBar,
            binding.mainVolumeSeekBar,
            binding.panSeekBar
        )
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Do you want to discard them?")
            .setPositiveButton("Discard") { _, _ -> findNavController().navigateUp() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupPlaybackControls() {
        binding.btnPlay.setOnClickListener {
            if (audioManager.isPlaying()) {
                audioManager.pause()
                lottieAnimationView.pauseAnimation()
            } else {
                audioManager.play()
                lottieAnimationView.playAnimation()
            }
            playbackHandler.post(updatePlaybackProgressRunnable)
        }

        binding.btnStop.setOnClickListener {
            audioManager.stop()
            lottieAnimationView.pauseAnimation()
            binding.playbackSeekBar.progress = 0
            playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
        }

        binding.btnPrevious.setOnClickListener {
            val tracks = audioManager.getAvailableTracks()
            if (tracks.isNotEmpty()) {
                currentTrackIndex = (currentTrackIndex - 1 + tracks.size) % tracks.size
                audioManager.selectTrack(currentTrackIndex)
                updateLottieAnimation(currentTrackIndex, true)
                playbackHandler.post(updatePlaybackProgressRunnable)
            }
        }

        binding.btnNext.setOnClickListener {
            val tracks = audioManager.getAvailableTracks()
            if (tracks.isNotEmpty()) {
                currentTrackIndex = (currentTrackIndex + 1) % tracks.size
                audioManager.selectTrack(currentTrackIndex)
                updateLottieAnimation(currentTrackIndex, true)
                playbackHandler.post(updatePlaybackProgressRunnable)
            }
        }

        binding.playbackSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let {
                    audioManager.seekTo(it)
                    playbackHandler.post(updatePlaybackProgressRunnable)
                }
            }
        })
    }

    /**
     * Exibe um diálogo para selecionar uma faixa de áudio da lista de faixas disponíveis.
     */
    private fun showTrackSelectionDialog() {
        val availableTracks = audioManager.getAvailableTracks()

        if (availableTracks.isEmpty()) {
            Toast.makeText(requireContext(), "No tracks available.", Toast.LENGTH_SHORT).show()
            return
        }

        // Converte a lista de Kotlin para um Array de CharSequence para o setItems
        val trackNamesArray = availableTracks.toTypedArray<CharSequence>()

        AlertDialog.Builder(requireContext())
            .setTitle("Select a Track")
            .setItems(trackNamesArray) { dialog, which ->
                // 'which' é o índice do item clicado
                audioManager.selectTrack(which)
                currentTrackIndex = which // Atualiza o índice da faixa atual
                updateLottieAnimation(currentTrackIndex, true) // Assume que a nova faixa começa a tocar
                playbackHandler.post(updatePlaybackProgressRunnable) // Reinicia/continua a atualização da seek bar
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateLottieAnimation(trackIndex: Int, isPlaying: Boolean) {
        val lottieResId = trackLottieMap[trackIndex] ?: R.raw.skull
        lottieAnimationView.setAnimation(lottieResId)

        if (isPlaying) {
            lottieAnimationView.playAnimation()
        } else {
            lottieAnimationView.pauseAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        aidlServiceManager.unbindService()
        playbackHandler.removeCallbacks(updatePlaybackProgressRunnable)
        lottieAnimationView.pauseAnimation()
    }

    companion object {
        private const val TAG = "UserPage"
    }
}
