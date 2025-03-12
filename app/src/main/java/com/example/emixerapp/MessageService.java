package com.example.emixerapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.reaj.emixer.IMessageService;


public class MessageService extends Service {

    // Implementação do Binder para a interface IMessageService
    private final IMessageService.Stub binder = new IMessageService.Stub() {
        @Override
        public void sendMessage(String message) throws RemoteException {
            Log.d("AIDL_DEMO", "Message received: " + message);
            // Lógica para lidar com a mensagem, por exemplo, exibir uma notificação
        }

        @Override
        public boolean setBass(int value) throws RemoteException {
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 10) {
                Log.d("AIDL_DEMO", "Setting Bass to: " + value);
                // Aqui você aplicaria a configuração de graves (Bass) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w("AIDL_DEMO", "Invalid Bass value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setMid(int value) throws RemoteException {
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 10) {
                Log.d("AIDL_DEMO", "Setting Mid to: " + value);
                // Aqui você aplicaria a configuração de médios (Mid) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w("AIDL_DEMO", "Invalid Mid value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setTreble(int value) throws RemoteException {
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 10) {
                Log.d("AIDL_DEMO", "Setting Treble to: " + value);
                // Aqui você aplicaria a configuração de agudos (Treble) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w("AIDL_DEMO", "Invalid Treble value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setMainVolume(int value) throws RemoteException {
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 100) {
                Log.d("AIDL_DEMO", "Setting Main Volume to: " + value);
                // Aqui você aplicaria a configuração de volume principal (MainVolume) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w("AIDL_DEMO", "Invalid Main Volume value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setPan(int value) throws RemoteException {
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= -100 && value <= 100) {
                Log.d("AIDL_DEMO", "Setting Pan to: " + value);
                // Aqui você aplicaria a configuração de balanço estéreo (Pan) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w("AIDL_DEMO", "Invalid Pan value: " + value);
                return false; // Indica falha
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // Retorna o Binder para que os clientes possam interagir com o serviço
        return binder;
    }
}
