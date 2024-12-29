package com.reaj.emixer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

// Classe que recebe notificações sobre mudanças no modo avião
class AirplaneModeBroadcastReceiver : BroadcastReceiver() {
    // Método chamado quando a transmissão é recebida
    override fun onReceive(context: Context?, intent: Intent?) {
        // Obtém o estado do modo avião da intenção recebida; padrão é false se não encontrado
        val isAirplaneModeOn =  intent?.getBooleanExtra("state", false) ?: return

        // Verifica se o modo avião está ativado
        if (isAirplaneModeOn){
            // Exibe uma mensagem informando que o modo avião está ativado
            Toast.makeText(context, "Airplane mode ON, won't sync across devices", Toast.LENGTH_LONG).show()
        } else {
            // Exibe uma mensagem informando que o modo avião está desativado
            Toast.makeText(context, "Airplane mode OFF, syncing across devices", Toast.LENGTH_LONG).show()
        }
    }
}