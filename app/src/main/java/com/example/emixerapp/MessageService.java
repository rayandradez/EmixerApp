package com.example.emixerapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.reaj.emixer.IMessageService;

// Declaração da classe MessageService, que herda da classe Service
public class MessageService extends Service {

    // Declaração e instanciação do binder, que permite a comunicação entre processos
    private final IMessageService.Stub binder = new IMessageService.Stub() {
        @Override
        public void sendMessage(String message) throws RemoteException {
            Log.d("AIDL_DEMO", "Message received: " + message);
        }
    };

    // Método chamado quando um aplicativo se conecta ao serviço
    @Override
    public IBinder onBind(Intent intent) {
        // Retorna o binder para permitir a comunicação
        return binder;
    }
}
