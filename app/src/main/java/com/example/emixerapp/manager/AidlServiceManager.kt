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
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Gerencia a vinculação e a comunicação com o serviço AIDL (Android Interface Definition Language).
 *
 * @param context O contexto da aplicação.
 */
class AidlServiceManager(private val context: Context) {

    private var messageService: IMessageService? = null // Interface AIDL para comunicação com o serviço
    private var isServiceBound = AtomicBoolean(false) // Flag atômica para indicar se o serviço está vinculado
    private var serviceConnection: ServiceConnection? = null // Objeto para gerenciar a conexão com o serviço

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
                // Chamado quando a conexão com o serviço é estabelecida
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    messageService = IMessageService.Stub.asInterface(service)  // Obtém a interface AIDL do serviço
                    isServiceBound.set(true) // Define a flag isServiceBound como true
                    Log.d("AidlServiceManager", "Service connected: ${name?.className}")
                    messageService?.let { onServiceConnected(it) } // Chama o callback onServiceConnected com a interface AIDL
                }

                // Chamado quando a conexão com o serviço é perdida
                override fun onServiceDisconnected(name: ComponentName?) {
                    messageService = null // Limpa a interface AIDL
                    isServiceBound = AtomicBoolean(false)  // Define a flag isServiceBound como false
                    Log.d("AidlServiceManager", "Service disconnected")
                    onServiceDisconnected() // Chama o callback onServiceDisconnected
                }
            }
        }

        // Cria uma Intent com a ação correta para iniciar o serviço
        val intent = Intent(context, MessageService::class.java)
        intent.action = MessageService.ACTION_FOREGROUND_SERVICE // Define a ação para o serviço de primeiro plano

        try {
            // Vincula ao serviço usando a Intent e a ServiceConnection
            context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            Log.e("AidlServiceManager", "SecurityException while binding service: ${e.message}")
        }
    }

    /**
     * Desvincula do serviço AIDL.
     */
    fun unbindService() {
        // Verifica se o serviço está vinculado e se a ServiceConnection existe
        if (isServiceBound.get() && serviceConnection != null) {
            try {
                context.unbindService(serviceConnection!!) // Desvincula do serviço
                isServiceBound.set(false) // Define a flag isServiceBound como false
                messageService = null // Limpa a interface AIDL
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
        return isServiceBound.get() // Retorna o valor da flag isServiceBound
    }

    /**
     * Retorna a interface AIDL.
     *
     * @return A interface IMessageService ou null se o serviço não estiver vinculado.
     */
    fun getMessageService(): IMessageService? {
        return messageService // Retorna a interface AIDL
    }
}
