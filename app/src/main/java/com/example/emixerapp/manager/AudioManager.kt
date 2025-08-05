// AudioManager.kt
package com.example.emixerapp.manager

import android.os.RemoteException
import android.util.Log
import com.reaj.emixer.IMessageService

class AudioManager(private val aidlServiceManager: AidlServiceManager) {

    private val TAG = "AudioManager"

    // Propriedade para acessar a instância do IMessageService
    private val messageService: IMessageService?
        get() = aidlServiceManager.messageService

    // --- Métodos de controle de áudio ---
    fun setBass(value: Int) {
        try {
            messageService?.setBass(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting bass: ${e.message}")
        }
    }

    fun setMid(value: Int) {
        try {
            messageService?.setMid(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting mid: ${e.message}")
        }
    }

    fun setTreble(value: Int) {
        try {
            messageService?.setTreble(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting treble: ${e.message}")
        }
    }

    fun setMainVolume(value: Int) {
        try {
            messageService?.setMainVolume(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting main volume: ${e.message}")
        }
    }

    fun setPan(value: Int) {
        try {
            messageService?.setPan(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting pan: ${e.message}")
        }
    }

    // --- NOVOS MÉTODOS DE CONTROLE DE REPRODUÇÃO ---

    fun play() {
        try {
            messageService?.play()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error playing audio: ${e.message}")
        }
    }

    fun pause() {
        try {
            messageService?.pause()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error pausing audio: ${e.message}")
        }
    }

    fun stop() {
        try {
            messageService?.stop()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error stopping audio: ${e.message}")
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            messageService?.seekTo(positionMs)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error seeking audio: ${e.message}")
        }
    }

    fun getCurrentPosition(): Int {
        return try {
            messageService?.getCurrentPosition() ?: 0
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting current position: ${e.message}")
            0
        }
    }

    fun getDuration(): Int {
        return try {
            messageService?.getDuration() ?: 0
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting duration: ${e.message}")
            0
        }
    }

    fun getAvailableTracks(): List<String> {
        return try {
            messageService?.getAvailableTracks() ?: emptyList()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting available tracks: ${e.message}")
            emptyList()
        }
    }

    fun selectTrack(trackIndex: Int) {
        try {
            messageService?.selectTrack(trackIndex)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error selecting track: ${e.message}")
        }
    }

    fun isPlaying(): Boolean { // ADICIONE ESTE MÉTODO
        return try {
            messageService?.isPlaying() ?: false
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting isPlaying state: ${e.message}")
            false
        }
    }


}
