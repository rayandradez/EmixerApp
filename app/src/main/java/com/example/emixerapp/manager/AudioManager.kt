package com.example.emixerapp.manager

import android.os.RemoteException
import android.util.Log
import com.reaj.emixer.IMessageService

/**
 * Gerencia todas as chamadas diretas de controle de áudio para o MessageService.
 * Esta classe atua como uma camada de abstração segura para evitar RemoteException
 * em toda a UI. Ela é um "passa-prato" direto para o serviço.
 */
class AudioManager(private val aidlServiceManager: AidlServiceManager) {

    private val service: IMessageService?
        get() = aidlServiceManager.messageService

    companion object {
        private const val TAG = "AudioManager"
    }

    // --- Controles de Reprodução ---

    fun play() {
        try {
            service?.play()
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar play: ${e.message}")
        }
    }

    fun pause() {
        try {
            service?.pause()
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar pause: ${e.message}")
        }
    }

    fun stop() {
        try {
            service?.stop()
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar stop: ${e.message}")
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            service?.seekTo(positionMs)
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar seekTo: ${e.message}")
        }
    }

    fun selectTrack(trackIndex: Int) {
        try {
            service?.selectTrack(trackIndex)
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar selectTrack: ${e.message}")
        }
    }

    // --- Controles de Configuração de Áudio ---

    fun setBass(value: Int) {
        try {
            service?.setBass(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar setBass: ${e.message}")
        }
    }

    fun setMid(value: Int) {
        try {
            service?.setMid(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar setMid: ${e.message}")
        }
    }

    fun setTreble(value: Int) {
        try {
            service?.setTreble(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar setTreble: ${e.message}")
        }
    }

    fun setMainVolume(value: Int) {
        try {
            service?.setMainVolume(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar setMainVolume: ${e.message}")
        }
    }

    /**
     * Define o pan (balanço) do áudio.
     * <<< CORREÇÃO CRÍTICA APLICADA AQUI >>>
     * @param panValue O valor final para o serviço, variando de -100 (esquerda) a 100 (direita).
     * Esta função não faz mais nenhuma conversão. Ela simplesmente passa o valor recebido.
     */
    fun setPan(panValue: Int) {
        try {
            Log.d(TAG, "Enviando valor de Pan para o serviço: $panValue")
            service?.setPan(panValue)
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar setPan: ${e.message}")
        }
    }


    // --- Getters de Estado ---

    fun isPlaying(): Boolean {
        return try {
            service?.isPlaying ?: false
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar isPlaying: ${e.message}")
            false
        }
    }

    fun getAvailableTracks(): List<String> {
        return try {
            service?.availableTracks ?: emptyList()
        } catch (e: RemoteException) {
            Log.e(TAG, "Erro ao chamar getAvailableTracks: ${e.message}")
            emptyList()
        }
    }
}
