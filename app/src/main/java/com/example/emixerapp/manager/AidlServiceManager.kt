package com.example.emixerapp.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.emixerapp.MessageService
import com.reaj.emixer.IMessageService

// Singleton para gerenciar a conexão com o serviço AIDL
object AidlServiceManager {

    private const val TAG = "AidlServiceManager"

    var messageService: IMessageService? = null
        private set

    private var isBound = false
    private var serviceConnection: ServiceConnection? = null
    private var applicationContext: Context? = null

    private val connectedCallbacks = mutableListOf<(IMessageService) -> Unit>()
    private val disconnectedCallbacks = mutableListOf<() -> Unit>()

    // Handler para postar execuções no thread principal de forma assíncrona
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    fun initialize(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }
    }

    fun addServiceConnectedCallback(callback: (IMessageService) -> Unit) {
        connectedCallbacks.add(callback)
        if (isBound && messageService != null) {
            // <<< CORREÇÃO CRÍTICA PARA EVITAR ANR >>>
            // Em vez de chamar o callback diretamente e bloquear o chamador (ex: onStart),
            // postamos a execução na fila de mensagens do UI thread.
            // Isso permite que o método onStart() termine imediatamente.
            mainThreadHandler.post {
                Log.d(TAG, "Notificando novo callback sobre conexão existente (assíncrono).")
                callback(messageService!!)
            }
        }
    }

    fun addServiceDisconnectedCallback(callback: () -> Unit) {
        disconnectedCallbacks.add(callback)
    }

    fun removeServiceConnectedCallback(callback: (IMessageService) -> Unit) {
        connectedCallbacks.remove(callback)
    }

    fun removeServiceDisconnectedCallback(callback: () -> Unit) {
        disconnectedCallbacks.remove(callback)
    }

    fun bindService() {
        if (applicationContext == null) {
            Log.e(TAG, "AidlServiceManager não inicializado! Chame initialize(context) primeiro.")
            return
        }

        // Se já estivermos no processo de vinculação, não faça nada.
        if (isBound || serviceConnection != null) return

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                messageService = IMessageService.Stub.asInterface(service)
                isBound = true
                Log.d(TAG, "Serviço AIDL conectado. Notificando callbacks.")
                // Notifica todos os callbacks que estavam esperando
                connectedCallbacks.forEach { it(messageService!!) }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                messageService = null
                isBound = false
                serviceConnection = null // Limpa a conexão para permitir nova vinculação
                Log.w(TAG, "Serviço AIDL desconectado.")
                disconnectedCallbacks.forEach { it() }
            }
        }

        val intent = Intent(applicationContext, MessageService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext?.startForegroundService(intent)
        } else {
            applicationContext?.startService(intent)
        }

        try {
            applicationContext?.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException ao vincular serviço: ${e.message}")
        }
    }

    fun unbindService() {
        if (isBound && serviceConnection != null && applicationContext != null) {
            applicationContext?.unbindService(serviceConnection!!)
            isBound = false
            messageService = null
            serviceConnection = null
            Log.d(TAG, "Serviço AIDL desvinculado.")
        }
    }

    fun isServiceBound(): Boolean {
        return isBound
    }
}
