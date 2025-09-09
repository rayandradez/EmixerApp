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
import com.reaj.emixer.IMessageService // Importe a interface AIDL
import com.reaj.emixer.ui.components.viewModels.MainViewModel
import com.reaj.emixer.databinding.ServiceAidlBinding

/**
 * Fragmento para testar e monitorar o serviço AIDL.
 */
class ServiceAIDL : Fragment() {

    private var _binding: ServiceAidlBinding? = null // Variável para armazenar a instância do ViewBinding
    private val binding get() = _binding!! // Obtém a instância do ViewBinding, garantindo que não seja nula
    private lateinit var viewModel: MainViewModel // ViewModel para gerenciar os dados da UI
    // Removido: private lateinit var aidlServiceManager: AidlServiceManager

    private lateinit var textViewMemoryUsage: TextView // TextView para exibir o uso de memória

    // Constantes para os valores de ganho a serem passados para o C++
    // 1000 = 0dB (flat)
    // 2000 = +6dB (boost)
    // 500 = -6dB (cut)
    private val GAIN_FLAT = 1000
    private val GAIN_BOOST = 2000
    private val GAIN_CUT = 500

    // Callback para conexão do serviço
    private val serviceConnectedCallback: (IMessageService) -> Unit = { service ->
        Log.d("ServiceAIDL", "Serviço AIDL conectado (callback Fragment).")
        // Agora você pode usar 'service' diretamente ou o AidlServiceManager.messageService
        updateValueOnScreen(service.getValue())
        updateUsageInfo()
    }

    // Callback para desconexão do serviço
    private val serviceDisconnectedCallback: () -> Unit = {
        Log.w("ServiceAIDL", "Serviço AIDL desconectado (callback Fragment).")
        binding.textViewValue.text = "Serviço Desconectado"
        textViewMemoryUsage.text = "Uso de Memória: N/A"
    }


    /**
     * Infla o layout do fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ServiceAidlBinding.inflate(inflater, container, false) // Infla o layout usando ViewBinding
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java] // Obtém uma instância do ViewModel
        // Removido: aidlServiceManager = AidlServiceManager(requireContext())
        return binding.root // Retorna a view raiz do layout
    }

    /**
     * Configura a UI e define os listeners dos botões.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewMemoryUsage = binding.textViewMemoryUsage // Obtém a referência para o TextView de uso de memória

        // Adiciona os callbacks para o AidlServiceManager
        AidlServiceManager.addServiceConnectedCallback(serviceConnectedCallback)
        AidlServiceManager.addServiceDisconnectedCallback(serviceDisconnectedCallback)

        // Tenta atualizar a UI imediatamente se o serviço já estiver vinculado
        AidlServiceManager.messageService?.let { service ->
            updateValueOnScreen(service.getValue())
            updateUsageInfo()
        }


        // Define o listener para o botão de atualizar valor
        binding.buttonUpdateValue.setOnClickListener {
            updateValue() // Chama o metodo para atualizar o valor
        }

        // Define o listener para o botão de enviar mensagem
        binding.BtnSendMessage.setOnClickListener {
            sendAIDLMessage() // Chama o método para enviar uma mensagem
        }

        // Define o listener para o botão de teste do HAL nativo
        binding.btnTriggerNativeHal.setOnClickListener {
            triggerNativeHalAudioWrite()
        }

        // Define o listener para o botão de testar equalizador nativo (apenas log)
        binding.btnTestNativeEqualizer.setOnClickListener {
            testNativeEqualizer()
        }

        // Define os listeners para os botões de equalização de áudio C++
        binding.btnPlayNativeEqualizedBassBoost.setOnClickListener {
            playNativeEqualizedAudio(gains = intArrayOf(GAIN_BOOST, GAIN_FLAT, GAIN_FLAT))
        }

        binding.btnPlayNativeEqualizedMidBoost.setOnClickListener {
            playNativeEqualizedAudio(gains = intArrayOf(GAIN_FLAT, GAIN_BOOST, GAIN_FLAT))
        }

        binding.btnPlayNativeEqualizedHighBoost.setOnClickListener {
            playNativeEqualizedAudio(gains = intArrayOf(GAIN_FLAT, GAIN_FLAT, GAIN_BOOST))
        }

        binding.btnPlayNativeEqualizedFlat.setOnClickListener {
            playNativeEqualizedAudio(gains = intArrayOf(GAIN_FLAT, GAIN_FLAT, GAIN_FLAT))
        }

        // Define o listener para o botão de parar a onda de teste C++
        binding.btnStopNativeEqualized.setOnClickListener {
            stopNativeEqualizedAudio()
        }

        // Removido: bindAidlService()
    }

    /**
     * Limpa a referência do ViewBinding e desvincula do serviço AIDL para evitar vazamentos de memória.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Remove os callbacks do AidlServiceManager para evitar vazamentos
        AidlServiceManager.removeServiceConnectedCallback(serviceConnectedCallback)
        AidlServiceManager.removeServiceDisconnectedCallback(serviceDisconnectedCallback)
        // Removido: unbindAidlService()
        _binding = null // Define a variável _binding como nula
    }


    // Removido: bindAidlService() e unbindAidlService() pois o gerenciamento é centralizado


    /**
     * Atualiza o valor do serviço.
     */
    private fun updateValue() {
        val aidlInterface = AidlServiceManager.messageService // <<< PEGA A INSTÂNCIA DO SINGLETON
        if (AidlServiceManager.isServiceBound() && aidlInterface != null) {
            try {
                val newValue = (0..100).random()
                aidlInterface.setValue(newValue)
                updateValueOnScreen(newValue)
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao chamar o serviço: ${e.message}")
                binding.textViewValue.text = "Erro: ${e.message}"
            }
        } else {
            Log.w("ServiceAIDL", "Serviço não vinculado")
            binding.textViewValue.text = "Serviço não vinculado"
        }
    }

    /**
     * Atualiza o valor do serviço na tela.
     *
     * @param value O valor do serviço a ser exibido.
     */
    private fun updateValueOnScreen(value: Int) {
        binding.textViewValue.text = "Valor do Serviço: $value"
    }

    /**
     * Atualiza as informações de uso do serviço na tela.
     */
    private fun updateUsageInfo() {
        val aidlInterface = AidlServiceManager.messageService // <<< PEGA A INSTÂNCIA DO SINGLETON
        if (AidlServiceManager.isServiceBound() && aidlInterface != null) {
            try {
                val memoryUsage = aidlInterface.memoryUsage

                textViewMemoryUsage.text = "Uso de Memória: $memoryUsage KB"
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao chamar o serviço: ${e.message}")
                textViewMemoryUsage.text = "Erro: ${e.message}"
            }
        } else {
            Log.w("ServiceAIDL", "Serviço não vinculado")
            textViewMemoryUsage.text = "Serviço não vinculado"
        }
    }

    /**
     * Envia uma mensagem para o serviço AIDL.
     */
    private fun sendAIDLMessage() {
        val aidlInterface = AidlServiceManager.messageService // <<< PEGA A INSTÂNCIA DO SINGLETON
        if (AidlServiceManager.isServiceBound() && aidlInterface != null) {
            try {
                aidlInterface.sendMessage("Hello from EMIXER AIDL!")
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao chamar o serviço: ${e.message}")
            }
        } else {
            Toast.makeText(context, "Service not bound", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Aciona a função HAL nativa através do serviço AIDL.
     */
    private fun triggerNativeHalAudioWrite() {
        val aidlInterface = AidlServiceManager.messageService // <<< PEGA A INSTÂNCIA DO SINGLETON
        if (AidlServiceManager.isServiceBound() && aidlInterface != null) {
            try {
                val result = aidlInterface.triggerNativeHalAudioWrite()
                Log.d("ServiceAIDL", "Resultado da chamada HAL nativa: $result")
                Toast.makeText(context, "HAL Nativa acionada. Resultado: $result", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao chamar HAL nativa via serviço: ${e.message}")
                Toast.makeText(context, "Erro ao acionar HAL nativa: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("ServiceAIDL", "Serviço não vinculado ao tentar acionar HAL nativa")
            Toast.makeText(context, "Serviço não vinculado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Testa a função de equalização nativa C++ através do serviço AIDL (apenas log).
     */
    private fun testNativeEqualizer() {
        val aidlInterface = AidlServiceManager.messageService // <<< PEGA A INSTÂNCIA DO SINGLETON
        if (AidlServiceManager.isServiceBound() && aidlInterface != null) {
            try {
                val gains = intArrayOf(GAIN_FLAT, GAIN_FLAT, GAIN_FLAT)
                val processedSamples = aidlInterface.applyNativeEqualizationTest(gains)
                Log.d("ServiceAIDL", "Equalizador Nativo (C++) acionado. Amostras processadas: $processedSamples")
                Toast.makeText(context, "Equalizador Nativo (C++) acionado. Amostras processadas: $processedSamples", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao chamar equalizador nativo via serviço: ${e.message}")
                Toast.makeText(context, "Erro ao testar equalizador nativo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("ServiceAIDL", "Serviço não vinculado ao tentar testar equalizador nativo")
            Toast.makeText(context, "Serviço não vinculado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Aciona a reprodução de áudio gerado e equalizado pelo C++.
     * @param gains Array de ganhos para Bass, Mid, High (ex: [1000, 1000, 1000] para flat).
     */
    private fun playNativeEqualizedAudio(gains: IntArray) {
        val aidlInterface = AidlServiceManager.messageService // <<< PEGA A INSTÂNCIA DO SINGLETON
        if (AidlServiceManager.isServiceBound() && aidlInterface != null) {
            try {
                val duration = 3 // Duração em segundos

                aidlInterface.playProcessedAudioNative(gains, duration)
                Toast.makeText(context, "Reproduzindo onda equalizada (Ganhos: ${gains.joinToString()})", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao tocar áudio equalizado nativo via serviço: ${e.message}")
                Toast.makeText(context, "Erro ao tocar áudio equalizado nativo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("ServiceAIDL", "Serviço não vinculado ao tentar tocar áudio equalizado nativo")
            Toast.makeText(context, "Serviço não vinculado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Para a reprodução de áudio gerado e equalizado pelo C++.
     */
    private fun stopNativeEqualizedAudio() {
        val aidlInterface = AidlServiceManager.messageService // <<< PEGA A INSTÂNCIA DO SINGLETON
        if (AidlServiceManager.isServiceBound() && aidlInterface != null) {
            try {
                aidlInterface.stopProcessedAudioNative()
                Toast.makeText(context, "Onda de teste parada.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ServiceAIDL", "Erro ao parar áudio equalizado nativo via serviço: ${e.message}")
                Toast.makeText(context, "Erro ao parar áudio equalizado nativo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("ServiceAIDL", "Serviço não vinculado ao tentar parar áudio equalizado nativo")
            Toast.makeText(context, "Serviço não vinculado", Toast.LENGTH_SHORT).show()
        }
    }
}
