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

        @Override
        public boolean setBass(int value) throws RemoteException {
            if (value >= 0 && value <= 10) { // Example validation
                Log.d("AIDL_DEMO", "Setting Bass to: " + value);
                // Here you would actually apply the bass setting
                return true; // Success
            } else {
                Log.w("AIDL_DEMO", "Invalid Bass value: " + value);
                return false; // Failure
            }
        }

        @Override
        public boolean setMid(int value) throws RemoteException {
            if (value >= 0 && value <= 10) { // Example validation
                Log.d("AIDL_DEMO", "Setting Mid to: " + value);
                // Here you would actually apply the mid setting
                return true; // Success
            } else {
                Log.w("AIDL_DEMO", "Invalid Mid value: " + value);
                return false; // Failure
            }
        }

        @Override
        public boolean setTreble(int value) throws RemoteException {
            if (value >= 0 && value <= 10) { // Example validation
                Log.d("AIDL_DEMO", "Setting Treble to: " + value);
                // Here you would actually apply the treble setting
                return true; // Success
            } else {
                Log.w("AIDL_DEMO", "Invalid Treble value: " + value);
                return false; // Failure
            }
        }

        @Override
        public boolean setMainVolume(int value) throws RemoteException {
            if (value >= 0 && value <= 100) { // Example validation
                Log.d("AIDL_DEMO", "Setting Main Volume to: " + value);
                // Here you would actually apply the main volume setting
                return true; // Success
            } else {
                Log.w("AIDL_DEMO", "Invalid Main Volume value: " + value);
                return false; // Failure
            }
        }

        @Override
        public boolean setPan(int value) throws RemoteException {
            if (value >= -100 && value <= 100) { // Example validation
                Log.d("AIDL_DEMO", "Setting Pan to: " + value);
                // Here you would actually apply the pan setting
                return true; // Success
            } else {
                Log.w("AIDL_DEMO", "Invalid Pan value: " + value);
                return false; // Failure
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
