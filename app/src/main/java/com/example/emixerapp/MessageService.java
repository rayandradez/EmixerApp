package com.example.emixerapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


import com.reaj.emixer.IMessageService;

public class MessageService extends Service {

    //implementação do Binder
    //criar uma instacia (objeto) binder

    private IMessageService.Stub binder = new IMessageService.Stub() {
        @Override
        public void sendMessage(String message) throws RemoteException {
            Log.d("MessageService", "Mensagem recebida" + message);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }



}
