package com.example.emixerapp.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.reaj.emixer.R;

public class NotificationModule implements NotificationInterface {

    private final Context context;
    private final String channelId;

    public NotificationModule(Context context, String channelId) {
        this.context = context;
        this.channelId = channelId;
    }

    @Override
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Emixer Service Channel", // Nome visível para o usuário
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public Notification createNotification(String title, String text) {
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.circle_users_adapter) // Use o seu ícone
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
