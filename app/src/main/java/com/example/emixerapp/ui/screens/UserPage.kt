package your_package_name.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // Get ViewModel
        sharedPreferences = requireActivity().getSharedPreferences("audio_settings", Context.MODE_PRIVATE)

   //     val user = args.user ?: UserModel() // Get the user object, or a default

     //   setSeekBars(user)
        loadAudioSettings()

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
                    // Atualize os elementos da UI com os dados coletados
                    // Exemplo:
                    binding.txtUserName.text = data.user?.name?.substringBefore(" ")
                    Log.e("FRAGMENT", "TESTING VIEW MODEL: $data")
                }
            }
        }

    }

    private fun saveAudioSettings() {
        val editor = sharedPreferences.edit()
        try {
            editor.putInt("bass", binding.bassSeekBar.progress)
            editor.putInt("middle", binding.midSeekBar.progress)
            editor.putInt("high", binding.highSeekBar.progress)
            editor.putInt("mainVolume", binding.mainVolumeSeekBar.progress)
            editor.putInt("pan", binding.panSeekBar.progress)
            editor.apply()
            Log.d("UserPage", "Audio settings saved successfully.")
        } catch (e: Exception) {
            Log.e("UserPage", "Error saving audio settings: ${e.message}")
        }
    }

    private fun setSeekBars(user: UserModel) {
        binding.bassSeekBar.progress = user.bass
        binding.midSeekBar.progress = user.middle
        binding.highSeekBar.progress = user.high
        binding.mainVolumeSeekBar.progress = user.mainVolume
        binding.panSeekBar.progress = user.pan
    }

    private fun loadAudioSettings() {
        binding.bassSeekBar.progress = sharedPreferences.getInt("bass", 0)
        binding.midSeekBar.progress = sharedPreferences.getInt("middle", 0)
        binding.highSeekBar.progress = sharedPreferences.getInt("high", 0)
        binding.mainVolumeSeekBar.progress = sharedPreferences.getInt("mainVolume", 50)
        binding.panSeekBar.progress = sharedPreferences.getInt("pan", 50)
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
