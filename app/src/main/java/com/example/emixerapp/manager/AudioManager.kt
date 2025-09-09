package com.example.emixerapp.manager

import android.os.RemoteException
import android.util.Log

class AudioManager(private val serviceManager: AidlServiceManager) {

    companion object {
        private const val TAG = "AudioManager"
    }

    fun play() {
        if (!serviceManager.isServiceBound()) {
            Log.w(TAG, "Play: Service not bound")
            return
        }
        try {
            serviceManager.messageService?.play()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error calling play", e)
        }
    }

    fun pause() {
        if (!serviceManager.isServiceBound()) {
            Log.w(TAG, "Pause: Service not bound")
            return
        }
        try {
            serviceManager.messageService?.pause()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error calling pause", e)
        }
    }

    fun stop() {
        if (!serviceManager.isServiceBound()) {
            Log.w(TAG, "Stop: Service not bound")
            return
        }
        try {
            serviceManager.messageService?.stop()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error calling stop", e)
        }
    }

    fun seekTo(positionMs: Int) {
        if (!serviceManager.isServiceBound()) return
        try {
            serviceManager.messageService?.seekTo(positionMs)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error calling seekTo", e)
        }
    }

    fun selectTrack(trackIndex: Int) {
        if (!serviceManager.isServiceBound()) return
        try {
            serviceManager.messageService?.selectTrack(trackIndex)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error calling selectTrack", e)
        }
    }

    fun isPlaying(): Boolean {
        if (!serviceManager.isServiceBound()) return false
        return try {
            serviceManager.messageService?.isPlaying ?: false
        } catch (e: RemoteException) {
            Log.e(TAG, "Error calling isPlaying", e)
            false
        }
    }

    fun getAvailableTracks(): List<String> {
        if (!serviceManager.isServiceBound()) return emptyList()
        return try {
            serviceManager.messageService?.availableTracks ?: emptyList()
        } catch (e: RemoteException) {
            Log.e(TAG, "Error calling getAvailableTracks", e)
            emptyList()
        }
    }

    // --- MÉTODOS DE CONTROLE DE ÁUDIO ---

    fun setBass(value: Int) {
        if (!serviceManager.isServiceBound()) return
        try {
            serviceManager.messageService?.setBass(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting bass", e)
        }
    }

    fun setMid(value: Int) {
        if (!serviceManager.isServiceBound()) return
        try {
            serviceManager.messageService?.setMid(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting mid", e)
        }
    }

    fun setTreble(value: Int) {
        if (!serviceManager.isServiceBound()) return
        try {
            serviceManager.messageService?.setTreble(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting treble", e)
        }
    }

    fun setMainVolume(value: Int) {
        if (!serviceManager.isServiceBound()) return
        try {
            serviceManager.messageService?.setMainVolume(value)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting main volume", e)
        }
    }

    fun setPan(progress: Int) {
        if (!serviceManager.isServiceBound()) return

        // Converte o progresso do slider (0-100) para o range do serviço (-100 a 100)
        val panValue = (progress * 2) - 100

        try {
            Log.d(TAG, "Setting Pan: Slider Progress=$progress -> Service Value=$panValue")
            serviceManager.messageService?.setPan(panValue)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error setting pan", e)
        }
    }
}
