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

/**
 * Fragmento para testar e monitorar o serviço AIDL.
 */
class ServiceAIDL : Fragment() {

    private var _binding: ServiceAidlBinding? = null // Variável para armazenar a instância do ViewBinding
    private val binding get() = _binding!! // Obtém a instância do ViewBinding, garantindo que não seja nula
    private lateinit var viewModel: MainViewModel // ViewModel para gerenciar os dados da UI
    private lateinit var aidlServiceManager: AidlServiceManager // Gerenciador para a comunicação com o serviço AIDL
    private lateinit var textViewMemoryUsage: TextView // TextView para exibir o uso de memória

    /**
     * Infla o layout do fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ServiceAidlBinding.inflate(inflater, container, false) // Infla o layout usando ViewBinding
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java] // Obtém uma instância do ViewModel
        aidlServiceManager = AidlServiceManager(requireContext()) // Initialize AidlServiceManager
        return binding.root // Retorna a view raiz do layout
    }

    /**
     * Configura a UI e define os listeners dos botões.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewMemoryUsage = binding.textViewMemoryUsage // Obtém a referência para o TextView de uso de memória

        // Define o listener para o botão de atualizar valor
        binding.buttonUpdateValue.setOnClickListener {
            updateValue() // Chama o metodo para atualizar o valor
        }

        // Define o listener para o botão de enviar mensagem
        binding.BtnSendMessage.setOnClickListener {
            sendAIDLMessage() // Chama o método para enviar uma mensagem
        }

        // NOVO: Define o listener para o botão de teste do HAL nativo
        binding.btnTriggerNativeHal.setOnClickListener { // <<< ADICIONADO AQUI
            triggerNativeHalAudioWrite()
        }

        bindAidlService() // Vincula ao serviço AIDL
    }

    /**
     * Limpa a referência do ViewBinding e desvincula do serviço AIDL para evitar vazamentos de memória.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        unbindAidlService() // Desvincula do serviço AIDL
        _binding = null // Define a variável _binding como nula
    }


    /**
     * Vincula ao serviço AIDL.
     */
    private fun bindAidlService() {
        aidlServiceManager.bindService(
            onServiceConnected = { aidlInterface ->
                Log.d("SettingsFragment", "Serviço AIDL conectado")
                updateValueOnScreen(aidlInterface.getValue())  // Atualiza o valor na tela
                updateUsageInfo()  // Atualiza as informações de uso
            },
            onServiceDisconnected = {
                Log.d("SettingsFragment", "Serviço AIDL desconectado")
                binding.textViewValue.text = "Serviço Desconectado"  // Atualiza o texto na tela
                textViewMemoryUsage.text = "Uso de Memória: N/A" // Atualiza o texto na tela
            }
        )
    }

    /**
     * Desvincula do serviço AIDL.
     */
    private fun unbindAidlService() {
        aidlServiceManager.unbindService() // Desvincula do serviço AIDL
    }

    /**
     * Atualiza o valor do serviço.
     */
    private fun updateValue() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.messageService
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
                binding.textViewValue.text = "Erro: ${e.message}"  // Atualiza o texto na tela
            }
        } else {
            Log.w("SettingsFragment", "Serviço não vinculado")
            binding.textViewValue.text = "Serviço não vinculado" // Atualiza o texto na tela
        }
    }

    /**
     * Atualiza o valor do serviço na tela.
     *
     * @param value O valor do serviço a ser exibido.
     */
    private fun updateValueOnScreen(value: Int) {
        binding.textViewValue.text = "Valor do Serviço: $value" // Atualiza o texto na tela
    }

    /**
     * Atualiza as informações de uso do serviço na tela.
     */
    private fun updateUsageInfo() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.messageService
                if (aidlInterface != null) {
                    val memoryUsage = aidlInterface.memoryUsage  // Obtém o uso de memória do serviço

                    textViewMemoryUsage.text = "Uso de Memória: $memoryUsage KB" // Atualiza o texto na tela
                } else {
                    Log.w("SettingsFragment", "Interface AIDL nula")
                    textViewMemoryUsage.text = "Interface AIDL nula" // Atualiza o texto na tela
                }
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Erro ao chamar o serviço: ${e.message}")
                textViewMemoryUsage.text = "Erro: ${e.message}" // Atualiza o texto na tela
            }
        } else {
            Log.w("SettingsFragment", "Serviço não vinculado")
            textViewMemoryUsage.text = "Serviço não vinculado" // Atualiza o texto na tela
        }


    }
    /**
     * Envia uma mensagem para o serviço AIDL.
     */
    private fun sendAIDLMessage() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.messageService
                if (aidlInterface != null) {
                    aidlInterface.sendMessage("Hello from EMIXER AIDL!") // Envia a mensagem
                } else {
                    Log.w("SettingsFragment", "Interface AIDL nula")
                }
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Erro ao chamar o serviço: ${e.message}") // Exibe uma mensagem
            }
        } else {
            Toast.makeText(context, "Service not bound", Toast.LENGTH_SHORT).show() // Exibe uma mensagem
        }
    }

    /**
     * Aciona a função HAL nativa através do serviço AIDL.
     */
    private fun triggerNativeHalAudioWrite() { // <<< ADICIONADO AQUI
        if (aidlServiceManager.isServiceBound()) {
            try {
                val aidlInterface = aidlServiceManager.messageService
                if (aidlInterface != null) {
                    val result = aidlInterface.triggerNativeHalAudioWrite()
                    Log.d("ServiceAIDL", "Resultado da chamada HAL nativa: $result")
                    Toast.makeText(context, "HAL Nativa acionada. Resultado: $result", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("ServiceAIDL", "Interface AIDL nula ao tentar acionar HAL nativa")
                }
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao chamar HAL nativa via serviço: ${e.message}")
                Toast.makeText(context, "Erro ao acionar HAL nativa: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("ServiceAIDL", "Serviço não vinculado ao tentar acionar HAL nativa")
            Toast.makeText(context, "Serviço não vinculado", Toast.LENGTH_SHORT).show()
        }
    }
}
