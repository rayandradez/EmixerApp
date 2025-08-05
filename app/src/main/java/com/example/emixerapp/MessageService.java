package com.example.emixerapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import android.os.Process;
import com.reaj.emixer.IMessageService;
import com.reaj.emixer.R;

import java.util.List;
import java.util.Arrays;

/**
 * Serviço para gerenciar a reprodução de áudio e a equalização.
 */
public class MessageService extends Service {

    private static final String TAG = "MessageService";
    private static final String CHANNEL_ID = "emixer_channel";
    private static final int NOTIFICATION_ID = 1;
    private int myValue = 0;

    private MediaPlayer mediaPlayer;
    private Equalizer equalizer;

    private int bassLevel = 0;
    private int midLevel = 0;
    private int trebleLevel = 0;
    private int mainVolume = 50;
    private int panValue = 0;

    private int currentTrackIndex = 0;
    private int[] audioTracks = {R.raw.test_audio, R.raw.skull_music};
    private String[] trackNames = {"Test Audio Track 1", "Another Audio Track 2"};

    private final IMessageService.Stub binder = new IMessageService.Stub() {
        @Override
        public void sendMessage(String message) throws RemoteException {
            Log.d(TAG, "sendMessage() chamado, message: " + message);
        }

        @Override
        public boolean setBass(int value) throws RemoteException {
            Log.d(TAG, "setBass() chamado, value: " + value);
            if (value >= 0 && value <= 10) {
                bassLevel = value;
                applyEqualizerSettings();
                return true;
            } else {
                Log.w(TAG, "Invalid Bass value: " + value);
                return false;
            }
        }

        @Override
        public boolean setMid(int value) throws RemoteException {
            Log.d(TAG, "setMid() chamado, value: " + value);
            if (value >= 0 && value <= 10) {
                midLevel = value;
                applyEqualizerSettings();
                return true;
            } else {
                Log.w(TAG, "Invalid Mid value: " + value);
                return false;
            }
        }

        @Override
        public boolean setTreble(int value) throws RemoteException {
            Log.d(TAG, "setTreble() chamado, value: " + value);
            if (value >= 0 && value <= 10) {
                trebleLevel = value;
                applyEqualizerSettings();
                return true;
            } else {
                Log.w(TAG, "Invalid Treble value: " + value);
                return false;
            }
        }

        @Override
        public boolean setMainVolume(int value) throws RemoteException {
            Log.d(TAG, "setMainVolume() chamado, value: " + value);
            if (value >= 0 && value <= 100) {
                mainVolume = value;
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(value / 100f, value / 100f);
                }
                return true;
            } else {
                Log.w(TAG, "Invalid Main Volume value: " + value);
                return false;
            }
        }

        @Override
        public boolean setPan(int value) throws RemoteException {
            Log.d(TAG, "setPan() chamado, value: " + value);
            if (value >= -100 && value <= 100) {
                panValue = value;
                applyPanSettings();
                return true;
            } else {
                Log.w(TAG, "Invalid Pan value: " + value);
                return false;
            }
        }

        @Override
        public int getValue() throws RemoteException {
            return myValue;
        }

        @Override
        public void setValue(int value) throws RemoteException {
            myValue = value;
        }

        @Override
        public long getMemoryUsage() throws RemoteException {
            return getAudioServiceMemoryUsage();
        }

        @Override
        public void play() throws RemoteException {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Log.d(TAG, "Playback started.");
            }
        }

        @Override
        public void pause() throws RemoteException {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Log.d(TAG, "Playback paused.");
            }
        }

        @Override
        public void stop() throws RemoteException {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.prepareAsync();
                mediaPlayer.seekTo(0);
                Log.d(TAG, "Playback stopped.");
            }
        }

        @Override
        public void seekTo(int positionMs) throws RemoteException {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(positionMs);
            }
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        @Override
        public int getDuration() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.getDuration();
            }
            return 0;
        }

        @Override
        public List<String> getAvailableTracks() throws RemoteException {
            return Arrays.asList(trackNames);
        }

        @Override
        public void selectTrack(int trackIndex) throws RemoteException {
            if (trackIndex >= 0 && trackIndex < audioTracks.length) {
                boolean wasPlaying = false;
                if (mediaPlayer != null) {
                    wasPlaying = mediaPlayer.isPlaying();
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                if (equalizer != null) {
                    equalizer.release();
                    equalizer = null;
                }

                currentTrackIndex = trackIndex;
                initializeMediaPlayer(audioTracks[currentTrackIndex]);

                if (wasPlaying) {
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                }

                Log.d(TAG, "Selected track: " + trackNames[currentTrackIndex]);

                if (mediaPlayer != null) {
                    int audioSessionId = mediaPlayer.getAudioSessionId();
                    equalizer = new Equalizer(0, audioSessionId);
                    equalizer.setEnabled(true);
                    setupEqualizerBands();
                    applyEqualizerSettings();
                    applyPanSettings();
                    mediaPlayer.setVolume(mainVolume / 100f, mainVolume / 100f);
                }
            } else {
                Log.w(TAG, "Invalid track index: " + trackIndex);
            }
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() chamado");
        createNotificationChannel();

        // Inicializa o MediaPlayer com a primeira faixa
        initializeMediaPlayer(audioTracks[currentTrackIndex]);




        // Inicializa o Equalizer (ainda depende do mediaPlayer estar criado)
        if (mediaPlayer != null) {
            int audioSessionId = mediaPlayer.getAudioSessionId();
            equalizer = new Equalizer(0, audioSessionId);
            equalizer.setEnabled(true);
            setupEqualizerBands();
            applyEqualizerSettings();
            applyPanSettings();
            mediaPlayer.setVolume(mainVolume / 100f, mainVolume / 100f);
        }
    }

    // Método auxiliar para inicializar o MediaPlayer
    private void initializeMediaPlayer(int audioResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, audioResId);
        mediaPlayer.setLooping(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() chamado, intent: " + intent);
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() chamado, intent: " + intent + ", flags: " + flags + ", startId: " + startId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.circle_users_adapter)
                .setContentTitle("Emixer App")
                .setContentText("Ajustando áudio em segundo plano")
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Notification notification = builder.build();
        Log.d(TAG, "Notificação criada");

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() chamado");

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
    }

    private void createNotificationChannel() {
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

    private void setupEqualizerBands() {
        if (equalizer == null) return;

        short numberOfBands = equalizer.getNumberOfBands();
        final short minEQLevel = equalizer.getBandLevelRange()[0];
        final short maxEQLevel = equalizer.getBandLevelRange()[1];

        Log.d(TAG, "Equalizer has " + numberOfBands + " bands.");
        Log.d(TAG, "Band level range: " + minEQLevel + " to " + maxEQLevel + "mB");

        for (short i = 0; i < numberOfBands; i++) {
            int centerFreq = equalizer.getCenterFreq(i);
            Log.d(TAG, "Band " + i + " center frequency: " + centerFreq + "mHz");
        }
    }

    private void applyEqualizerSettings() {
        if (equalizer == null) return;

        short numberOfBands = equalizer.getNumberOfBands();
        final short minEQLevel = equalizer.getBandLevelRange()[0];

        if (numberOfBands > 0) {
            short level = (short) (minEQLevel + (bassLevel * 100));
            try {
                equalizer.setBandLevel((short) 0, level);
                Log.d(TAG, "Set Bass (Band 0) to " + level + "mB (user: " + bassLevel + ")");
            } catch (IllegalArgumentException | IllegalStateException e) {
                Log.e(TAG, "Error setting Bass band level: " + e.getMessage());
            }
        }

        if (numberOfBands > 1) {
            short level = (short) (minEQLevel + (midLevel * 100));
            try {
                equalizer.setBandLevel((short) 1, level);
                Log.d(TAG, "Set Mid (Band 1) to " + level + "mB (user: " + midLevel + ")");
            } catch (IllegalArgumentException | IllegalStateException e) {
                Log.e(TAG, "Error setting Mid band level: " + e.getMessage());
            }
        }

        if (numberOfBands > 2) {
            short level = (short) (minEQLevel + (trebleLevel * 100));
            try {
                equalizer.setBandLevel((short) 2, level);
                Log.d(TAG, "Set Treble (Band 2) to " + level + "mB (user: " + trebleLevel + ")");
            } catch (IllegalArgumentException | IllegalStateException e) {
                Log.e(TAG, "Error setting Treble band level: " + e.getMessage());
            }
        }
    }

    private void applyPanSettings() {
        if (mediaPlayer != null) {
            float panLeft = 1f;
            float panRight = 1f;

            if (panValue > 0) {
                panLeft = 1 - (panValue / 100f);
            } else if (panValue < 0) {
                panRight = 1 + (panValue / 100f);
            }

            mediaPlayer.setVolume(panLeft, panRight);
        }
    }

    private long getAudioServiceMemoryUsage() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            int pid = Process.myPid();
            Debug.MemoryInfo[] memInfo = am.getProcessMemoryInfo(new int[]{pid});
            if (memInfo != null && memInfo.length > 0) {
                return memInfo[0].getTotalPss();
            }
        }
        return 0;
    }
}
