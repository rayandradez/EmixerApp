package com.example.emixerapp.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.emixerapp.MessageService
import com.reaj.emixer.IMessageService // Importe a interface AIDL

// Transformando AidlServiceManager em um singleton
object AidlServiceManager { // <<< MUDANÇA AQUI: de class para object

    private val TAG = "AidlServiceManager"

    // O IMessageService agora é gerenciado por este singleton
    var messageService: IMessageService? = null
        private set // Apenas AidlServiceManager pode definir este valor

    private var isBound = false
    private var serviceConnection: ServiceConnection? = null
    private var applicationContext: Context? = null // Para vincular ao contexto da aplicação

    // O onServiceConnectedCallback e onServiceDisconnectedCallback agora são listas
    // para que múltiplos componentes possam ser notificados
    private val connectedCallbacks = mutableListOf<(IMessageService) -> Unit>()
    private val disconnectedCallbacks = mutableListOf<() -> Unit>()

    // Método para inicializar o manager com o contexto da aplicação
    fun initialize(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext // Usar contexto da aplicação para evitar vazamentos
        }
    }

    // Adiciona um callback para quando o serviço estiver conectado
    fun addServiceConnectedCallback(callback: (IMessageService) -> Unit) {
        connectedCallbacks.add(callback)
        if (isBound && messageService != null) {
            // Se já estiver vinculado, chame o callback imediatamente
            callback(messageService!!)
        }
    }

    // Adiciona um callback para quando o serviço estiver desconectado
    fun addServiceDisconnectedCallback(callback: () -> Unit) {
        disconnectedCallbacks.add(callback)
    }

    // Remove um callback
    fun removeServiceConnectedCallback(callback: (IMessageService) -> Unit) {
        connectedCallbacks.remove(callback)
    }

    // Remove um callback
    fun removeServiceDisconnectedCallback(callback: () -> Unit) {
        disconnectedCallbacks.remove(callback)
    }


    fun bindService() { // <<< REMOVIDO O PARÂMETRO context
        if (applicationContext == null) {
            Log.e(TAG, "AidlServiceManager não inicializado! Chame initialize(context) primeiro.")
            return
        }

        if (isBound && messageService != null) {
            Log.d(TAG, "Service already bound, calling onServiceConnected callbacks immediately.")
            messageService?.let { service ->
                connectedCallbacks.forEach { it(service) } // Chamar todos os callbacks
            }
            return
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                messageService = IMessageService.Stub.asInterface(service)
                isBound = true
                Log.d(TAG, "AIDL Service connected.")
                messageService?.let { serviceInstance ->
                    connectedCallbacks.forEach { it(serviceInstance) } // Chamar todos os callbacks
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                messageService = null
                isBound = false
                Log.w(TAG, "AIDL Service disconnected.")
                disconnectedCallbacks.forEach { it() } // Chamar todos os callbacks
            }
        }

        val intent = Intent(applicationContext, MessageService::class.java)
        try {
            applicationContext?.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when binding service: ${e.message}")
            // Trate problemas de permissão ou outros problemas de segurança aqui
        }
    }

    fun unbindService() {
        if (isBound && serviceConnection != null && applicationContext != null) {
            applicationContext?.unbindService(serviceConnection!!)
            isBound = false
            messageService = null
            serviceConnection = null
            Log.d(TAG, "AIDL Service unbound.")
        }
    }

    fun isServiceBound(): Boolean {
        return isBound
    }
}
