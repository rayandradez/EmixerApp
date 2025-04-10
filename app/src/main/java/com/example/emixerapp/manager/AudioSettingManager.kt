// AudioSettingsManager.kt
package com.example.emixerapp.manager

import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import com.example.emixerapp.ui.screens.UserPage
import com.reaj.emixer.IMessageService
import java.lang.ref.WeakReference

/**
 * Gerencia as configurações de áudio, incluindo a configuração dos listeners das SeekBars e a notificação ao AudioManager.
 *
 * @param userPage Uma referência fraca para a UserPage, para evitar vazamentos de memória.
 * @param messageService A interface AIDL para comunicação com o serviço.
 * @param isServiceBound Indica se o serviço AIDL está vinculado.
 * @param onSettingsChanged Um callback chamado quando as configurações de áudio são alteradas.
 * @param onBassChanged Um callback chamado quando o valor do Bass é alterado.
 * @param onMidChanged Um callback chamado quando o valor do Mid é alterado.
 * @param onTrebleChanged Um callback chamado quando o valor do Treble é alterado.
 * @param onMainVolumeChanged Um callback chamado quando o valor do Volume Principal é alterado.
 * @param onPanChanged Um callback chamado quando o valor do Pan é alterado.
 */
class AudioSettingsManager(
    private val userPage: WeakReference<UserPage>,
    private val messageService: IMessageService?,
    private val isServiceBound: Boolean,
    private val onSettingsChanged: () -> Unit,
    private val onBassChanged: (Int) -> Unit,
    private val onMidChanged: (Int) -> Unit,
    private val onTrebleChanged: (Int) -> Unit,
    private val onMainVolumeChanged: (Int) -> Unit,
    private val onPanChanged: (Int) -> Unit,
    private val playAudio: () -> Unit,
    private val stopAudio: () -> Unit,
    private val pauseAudio: () -> Unit,

) {

    /**
     * Configura os listeners para cada SeekBar de ajuste de áudio.
     *
     * @param bassSeekBar SeekBar para ajustar o Bass.
     * @param midSeekBar SeekBar para ajustar o Mid.
     * @param highSeekBar SeekBar para ajustar o Treble.
     * @param mainVolumeSeekBar SeekBar para ajustar o Volume Principal.
     * @param panSeekBar SeekBar para ajustar o Pan.
     */
    fun setupSeekBarListeners(
        bassSeekBar: SeekBar,
        midSeekBar: SeekBar,
        highSeekBar: SeekBar,
        mainVolumeSeekBar: SeekBar,
        panSeekBar: SeekBar,
        btnPlay: Button,
        btnStop: Button,
        btnPause: Button
    ) {
        bassSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onSettingsChanged() // Indica que as configurações foram alteradas
                    onBassChanged(progress) // Notifica a mudança no Bass
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {} // Não precisa implementar
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}  // Não precisa implementar
        })

        midSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onSettingsChanged() // Indica que as configurações foram alteradas
                    onMidChanged(progress) // Notifica a mudança no Mid
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {} // Não precisa implementar
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}  // Não precisa implementar
        })

        highSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onSettingsChanged() // Indica que as configurações foram alteradas
                    onTrebleChanged(progress) // Notifica a mudança no Treble
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {} // Não precisa implementar
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}  // Não precisa implementar
        })

        mainVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onSettingsChanged() // Indica que as configurações foram alteradas
                    onMainVolumeChanged(progress) // Notifica a mudança no Volume Principal
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {} // Não precisa implementar
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}  // Não precisa implementar
        })

        panSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    onSettingsChanged() // Indica que as configurações foram alteradas
                    onPanChanged(progress) // Notifica a mudança no Pan
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {} // Não precisa implementar
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}  // Não precisa implementar
        })

        btnPlay.setOnClickListener {
            playAudio()
        }

        btnStop.setOnClickListener {
            stopAudio()
        }

        btnPause.setOnClickListener {
            pauseAudio()
        }

    }

    /**
     * Redefine as configurações de áudio para os valores padrão.
     *
     * @param bassSeekBar SeekBar para ajustar o Bass.
     * @param midSeekBar SeekBar para ajustar o Mid.
     * @param highSeekBar SeekBar para ajustar o Treble.
     * @param mainVolumeSeekBar SeekBar para ajustar o Volume Principal.
     * @param panSeekBar SeekBar para ajustar o Pan.
     */
    fun resetToDefaults(
        bassSeekBar: SeekBar,
        midSeekBar: SeekBar,
        highSeekBar: SeekBar,
        mainVolumeSeekBar: SeekBar,
        panSeekBar: SeekBar
    ) {
        bassSeekBar.progress = 0
        midSeekBar.progress = 0
        highSeekBar.progress = 0
        mainVolumeSeekBar.progress = 50
        panSeekBar.progress = 50
    }

}
