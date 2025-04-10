// AudioManager.kt

package com.example.emixerapp.manager

import android.util.Log

/**
 * Gerencia as configurações de áudio através do serviço AIDL.
 *
 * @param aidlServiceManager O manager para a vinculação e comunicação com o serviço AIDL.
 */
class AudioManager(private val aidlServiceManager: AidlServiceManager) {

    /**
     * Define o valor do Bass através do serviço AIDL.
     *
     * @param value O valor do Bass a ser definido.
     */
    fun setBass(value: Int) {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.setBass(value) ?: false
                if (success) {
                    Log.d("AudioManager", "Setting Bass to $value: Success")
                } else {
                    Log.w("AudioManager", "Setting Bass to $value: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }

    /**
     * Define o valor do Mid através do serviço AIDL.
     *
     * @param value O valor do Mid a ser definido.
     */
    fun setMid(value: Int) {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.setMid(value) ?: false
                if (success) {
                    Log.d("AudioManager", "Setting Mid to $value: Success")
                } else {
                    Log.w("AudioManager", "Setting Mid to $value: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }

    /**
     * Define o valor do Treble através do serviço AIDL.
     *
     * @param value O valor do Treble a ser definido.
     */
    fun setTreble(value: Int) {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.setTreble(value) ?: false
                if (success) {
                    Log.d("AudioManager", "Setting Treble to $value: Success")
                } else {
                    Log.w("AudioManager", "Setting Treble to $value: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }

    /**
     * Define o valor do Volume Principal através do serviço AIDL.
     *
     * @param value O valor do Volume Principal a ser definido.
     */
    fun setMainVolume(value: Int) {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.setMainVolume(value) ?: false
                if (success) {
                    Log.d("AudioManager", "Setting MainVolume to $value: Success")
                } else {
                    Log.w("AudioManager", "Setting MainVolume to $value: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }

    /**
     * Define o valor do Pan através do serviço AIDL.
     *
     * @param value O valor do Pan a ser definido.
     */
    fun setPan(value: Int) {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.setPan(value) ?: false
                if (success) {
                    Log.d("AudioManager", "Setting Pan to $value: Success")
                } else {
                    Log.w("AudioManager", "Setting Pan to $value: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }

    fun playAudio() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.playAudio() ?: false
                if (success) {
                    Log.d("AudioManager", "Setting play audio: Success")
                } else {
                    Log.w("AudioManager", "Setting play audio: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }

    fun pauseAudio() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.pauseAudio() ?: false
                if (success) {
                    Log.d("AudioManager", "Setting pause: Success")
                } else {
                    Log.w("AudioManager", "Setting pause: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }

    fun stopAudio() {
        if (aidlServiceManager.isServiceBound()) {
            try {
                val success = aidlServiceManager.getMessageService()?.stopAudio() ?: false
                if (success) {
                    Log.d("AudioManager", "Setting stop audio: Success")
                } else {
                    Log.w("AudioManager", "Setting stop audio: Failed (invalid value?)")
                }
            } catch (e: android.os.RemoteException) {
                Log.e("AudioManager", "RemoteException: ${e.message}")
            }
        } else {
            Log.w("AudioManager", "Service not bound")
        }
    }
}
