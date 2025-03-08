package com.example.emixerapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.reaj.emixer.IMessageService;

public class MessageService extends Service {

    private final IMessageService.Stub binder = new IMessageService.Stub() {
        @Override
        public void sendMessage(String message) throws RemoteException {
            Log.d("AIDL_DEMO", "Message received: " + message);
            // Handle the message, e.g., show a notification
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
