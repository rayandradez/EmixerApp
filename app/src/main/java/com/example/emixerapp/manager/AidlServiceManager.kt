// AidlServiceManager.kt
package com.example.emixerapp.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.reaj.emixer.IMessageService
import com.example.emixerapp.MessageService
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

class AidlServiceManager(private val context: Context) {

    private var messageService: IMessageService? = null
    private var isServiceBound = AtomicBoolean(false)
    private var serviceConnection: ServiceConnection? = null

    /**
     * Vincula ao serviço AIDL.
     *
     * @param onServiceConnected Callback chamado quando o serviço é conectado.
     * @param onServiceDisconnected Callback chamado quando o serviço é desconectado.
     */
    fun bindService(onServiceConnected: (IMessageService) -> Unit, onServiceDisconnected: () -> Unit) {
        // Cria uma nova ServiceConnection se ainda não existir
        if (serviceConnection == null) {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    messageService = IMessageService.Stub.asInterface(service)
                    isServiceBound.set(true)
                    Log.d("AidlServiceManager", "Service connected: ${name?.className}")
                    messageService?.let { onServiceConnected(it) }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    messageService = null
                    isServiceBound = AtomicBoolean(false)
                    Log.d("AidlServiceManager", "Service disconnected")
                    onServiceDisconnected()
                }
            }
        }

        // Cria uma Intent com a ação correta
        val intent = Intent(context, MessageService::class.java)
        intent.action = MessageService.ACTION_FOREGROUND_SERVICE // Use a string literal

        try {
            // Vincula ao serviço
            context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            Log.e("AidlServiceManager", "SecurityException while binding service: ${e.message}")
        }
    }

    /**
     * Desvincula do serviço AIDL.
     */
    fun unbindService() {
        if (isServiceBound.get() && serviceConnection != null) {
            try {
                context.unbindService(serviceConnection!!)
                isServiceBound.set(false)
                messageService = null
                Log.d("AidlServiceManager", "Service unbound")
            } catch (e: IllegalArgumentException) {
                Log.e("AidlServiceManager", "IllegalArgumentException while unbinding service: ${e.message}")
            }
        }
    }

    /**
     * Verifica se o serviço está vinculado.
     *
     * @return true se o serviço está vinculado, false caso contrário.
     */
    fun isServiceBound(): Boolean {
        return isServiceBound.get()
    }

    /**
     * Retorna a interface AIDL.
     *
     * @return A interface IMessageService ou null se o serviço não estiver vinculado.
     */
    fun getMessageService(): IMessageService? {
        return messageService
    }
}
