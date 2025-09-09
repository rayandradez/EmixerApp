package com.example.emixerapp.notification;


import android.app.Notification;

public interface NotificationInterface {
    void createNotificationChannel();
    Notification createNotification(String title, String text);
}
