package your_package_name.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    //Please, fix the navigation @joao
    //  private val args: UserPageArgs by navArgs()
    private var _binding: FragmentUserPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // Get ViewModel

        binding.saveAudioSettingsButton.setOnClickListener {
            saveAudioSettings()
            findNavController().navigate(R.id.action_userPage_to_welcome)
        }

        binding.resetAudioSettingsButton.setOnClickListener {
            resetToDefaults()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
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
    }

    private fun resetToDefaults() {
        binding.bassSeekBar.progress = 0
        binding.midSeekBar.progress = 0
        binding.highSeekBar.progress = 0
        binding.mainVolumeSeekBar.progress = 50
        binding.panSeekBar.progress = 50
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
