// AidlServiceManager.kt
package com.example.emixerapp.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.emixerapp.MessageService
import com.reaj.emixer.IMessageService // Importe a interface AIDL

class AidlServiceManager(private val context: Context) {

    private val TAG = "AidlServiceManager"
    // Torna messageService público para que AudioManager possa acessá-lo
    var messageService: IMessageService? = null
        private set // Apenas AidlServiceManager pode definir este valor

    private var isBound = false
    private var serviceConnection: ServiceConnection? = null

    fun bindService(
        onServiceConnected: (IMessageService) -> Unit,
        onServiceDisconnected: () -> Unit
    ) {
        if (isBound && messageService != null) {
            Log.d(TAG, "Service already bound, calling onServiceConnected immediately.")
            onServiceConnected(messageService!!) // Chama o callback imediatamente se já estiver vinculado
            return
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                messageService = IMessageService.Stub.asInterface(service)
                isBound = true
                Log.d(TAG, "AIDL Service connected.")
                messageService?.let { onServiceConnected(it) } // Chama o callback com a instância do serviço
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                messageService = null
                isBound = false
                Log.w(TAG, "AIDL Service disconnected.")
                onServiceDisconnected() // Chama o callback de desconexão
            }
        }

        val intent = Intent(context, MessageService::class.java)
        try {
            context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when binding service: ${e.message}")
            // Trate problemas de permissão ou outros problemas de segurança aqui
        }
    }

    fun unbindService() {
        if (isBound && serviceConnection != null) {
            context.unbindService(serviceConnection!!)
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
