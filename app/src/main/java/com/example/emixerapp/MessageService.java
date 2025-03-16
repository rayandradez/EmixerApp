package com.example.emixerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.reaj.emixer.IMessageService;
import com.reaj.emixer.R;


public class MessageService extends Service {

    private static final String TAG = "MessageService";
    private static final String CHANNEL_ID = "emixer_channel";
    private static final int NOTIFICATION_ID = 1; // ID único para a notificação
    public static final String ACTION_FOREGROUND_SERVICE = "com.example.emixerapp.action.FOREGROUND_SERVICE"; // Defina a ação aqui
    private int myValue = 0; // Adicione esta variável para armazenar o valor


    // Implementação do Binder para a interface IMessageService
    private final IMessageService.Stub binder = new IMessageService.Stub() {
        @Override
        public void sendMessage(String message) throws RemoteException {
            Log.d(TAG, "sendMessage() chamado, message: " + message);
            // Lógica para lidar com a mensagem, por exemplo, exibir uma notificação
        }

        @Override
        public boolean setBass(int value) throws RemoteException {
            Log.d(TAG, "setBass() chamado, value: " + value);
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 10) {
                Log.d(TAG, "Setting Bass to: " + value);
                // Aqui você aplicaria a configuração de graves (Bass) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w(TAG, "Invalid Bass value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setMid(int value) throws RemoteException {
            Log.d(TAG, "setMid() chamado, value: " + value);
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 10) {
                Log.d(TAG, "Setting Mid to: " + value);
                // Aqui você aplicaria a configuração de médios (Mid) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w(TAG, "Invalid Mid value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setTreble(int value) throws RemoteException {
            Log.d(TAG, "setTreble() chamado, value: " + value);
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 10) {
                Log.d(TAG, "Setting Treble to: " + value);
                // Aqui você aplicaria a configuração de agudos (Treble) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w(TAG, "Invalid Treble value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setMainVolume(int value) throws RemoteException {
            Log.d(TAG, "setMainVolume() chamado, value: " + value);
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= 0 && value <= 100) {
                Log.d(TAG, "Setting Main Volume to: " + value);
                // Aqui você aplicaria a configuração de volume principal (MainVolume) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w(TAG, "Invalid Main Volume value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public boolean setPan(int value) throws RemoteException {
            Log.d(TAG, "setPan() chamado, value: " + value);
            // Validação de exemplo: verifica se o valor está dentro do intervalo aceitável
            if (value >= -100 && value <= 100) {
                Log.d(TAG, "Setting Pan to: " + value);
                // Aqui você aplicaria a configuração de balanço estéreo (Pan) usando a API de áudio
                return true; // Indica sucesso
            } else {
                Log.w(TAG, "Invalid Pan value: " + value);
                return false; // Indica falha
            }
        }

        @Override
        public int getValue() throws RemoteException {
            Log.d(TAG, "getValue() chamado, retornando: " + myValue);
            return myValue;
        }

        @Override
        public void setValue(int value) throws RemoteException { // Modifique este método
            Log.d(TAG, "setValue() chamado, definindo valor para: " + value);
            myValue = value; // Atualiza o valor interno
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() chamado");
        createNotificationChannel(); // Cria o canal de notificação
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() chamado, intent: " + intent);
        // Retorna o Binder para que os clientes possam interagir com o serviço
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() chamado, intent: " + intent + ", flags: " + flags + ", startId: " + startId);

        // Cria uma notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.circle_users_adapter) // Substitua pelo seu ícone
                .setContentTitle("Emixer App")
                .setContentText("Ajustando áudio em segundo plano")
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Notification notification = builder.build();
        Log.d(TAG, "Notificação criada");

        // Inicia o serviço de primeiro plano
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY; // Garante que o serviço seja reiniciado se for interrompido
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() chamado");
    }


    private void createNotificationChannel() {
        // Cria um canal de notificação (necessário para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emixer Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notificação criado, ID: " + CHANNEL_ID);
            } else {
                Log.e(TAG, "NotificationManager não disponível");
            }
        }
    }
}
