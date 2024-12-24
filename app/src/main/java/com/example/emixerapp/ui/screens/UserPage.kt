package your_package_name.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private val AUDIO_PERMISSION_REQUEST = 100 // Constant for permission request code


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
                // Obtain the ViewModel to manage UI state
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // Get ViewModel

        // Check and request necessary audio permissions based on API level
        checkAudioPermissions()

        // Set click listener to save audio settings
        binding.saveAudioSettingsButton.setOnClickListener {
            saveAudioSettings()
            findNavController().navigate(R.id.action_userPage_to_welcome)
        }

        // Set click listener to reset audio settings to default values
        binding.resetAudioSettingsButton.setOnClickListener {
            resetToDefaults()
        }

        // Observe the UI state and update the UI components
        viewLifecycleOwner.lifecycleScope.launch {
            // Repeat this block of code whenever the lifecycle is in the STARTED state
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect data from the ViewModel's UI state flow
                viewModel.uiState.collect { data ->
                    // Update UI components with user data
                    binding.txtUserName.text = data.user?.name?.substringBefore(" ")
                    binding.bassSeekBar.progress = data.user?.bass!!
                    binding.midSeekBar.progress = data.user!!.middle
                    binding.highSeekBar.progress = data.user!!.high
                    binding.mainVolumeSeekBar.progress = data.user!!.mainVolume
                    binding.panSeekBar.progress = data.user!!.pan
                }
            }
        }

    }

    private fun saveAudioSettings() {
        // Update and save the user's audio settings in the ViewModel
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
        // Notify the user that settings have been saved
        Toast.makeText(requireContext(), "Audio settings saved (simulated)", Toast.LENGTH_SHORT)
            .show()
    }

    private fun resetToDefaults() {
        // Reset all audio settings to default values
        binding.bassSeekBar.progress = 0
        binding.midSeekBar.progress = 0
        binding.highSeekBar.progress = 0
        binding.mainVolumeSeekBar.progress = 50
        binding.panSeekBar.progress = 50
    }

    private fun checkAudioPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For API level 33 and above, use READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO
                    ),
                    AUDIO_PERMISSION_REQUEST
                )
            }
        } else {
            // For API levels below 33, consider using older permissions like RECORD_AUDIO if applicable
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
        _binding = null // Clear binding reference to prevent memory leaks
    }
}
