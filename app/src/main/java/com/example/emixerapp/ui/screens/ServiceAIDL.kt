package com.example.emixerapp.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.emixerapp.manager.AidlServiceManager
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.databinding.ServiceAidlBinding

class ServiceAIDL : Fragment() {

    private var _binding: ServiceAidlBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var aidlServiceManager: AidlServiceManager
    private lateinit var textViewMemoryUsage: TextView
    private lateinit var textViewCpuUsage: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ServiceAidlBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        aidlServiceManager = AidlServiceManager(requireContext()) // Initialize AidlServiceManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewMemoryUsage = binding.textViewMemoryUsage

        binding.buttonUpdateValue.setOnClickListener {
            updateValue()
        }

        binding.BtnSendMessage.setOnClickListener {
            sendAIDLMessage()
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
                updateUsageInfo()
            },
            onServiceDisconnected = {
                Log.d("SettingsFragment", "Serviço AIDL desconectado")
                binding.textViewValue.text = "Serviço Desconectado"
                textViewMemoryUsage.text = "Uso de Memória: N/A"
                textViewCpuUsage.text = "Uso de CPU: N/A"
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

    private fun updateUsageInfo() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.getMessageService()
                if (aidlInterface != null) {
                    val memoryUsage = aidlInterface.memoryUsage

                    textViewMemoryUsage.text = "Uso de Memória: $memoryUsage KB"
                } else {
                    Log.w("SettingsFragment", "Interface AIDL nula")
                    textViewMemoryUsage.text = "Interface AIDL nula"
                }
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Erro ao chamar o serviço: ${e.message}")
                textViewMemoryUsage.text = "Erro: ${e.message}"
            }
        } else {
            Log.w("SettingsFragment", "Serviço não vinculado")
            textViewMemoryUsage.text = "Serviço não vinculado"
        }


    }

    private fun sendAIDLMessage() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.getMessageService()
                if (aidlInterface != null) {
                    aidlInterface.sendMessage("Hello from EMIXER AIDL!")
                } else {
                    Log.w("SettingsFragment", "Interface AIDL nula")
                }
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Erro ao chamar o serviço: ${e.message}")
            }
        } else {
            Toast.makeText(context, "Service not bound", Toast.LENGTH_SHORT).show()
        }
    }

}
