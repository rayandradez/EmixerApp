package com.example.emixerapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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

import com.example.emixerapp.equalization.NativeEqualizerProcessor;

public class MessageService extends Service {

    private static final String TAG = "MessageService";
    private static final String CHANNEL_ID = "emixer_channel";
    private static final int NOTIFICATION_ID = 1;
    private int myValue = 0;

    static {
        try {
            System.loadLibrary("emixer");
            Log.d(TAG, "Native library 'emixer' loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library 'emixer': " + e.getMessage());
        }
    }

    public native int triggerHalAudioWrite();

    private NativeEqualizerProcessor nativeEqualizerProcessor;
    private MediaPlayer mediaPlayer;
    private Equalizer equalizer;
    private AudioTrack audioTrackNative;

    private final int SAMPLE_RATE = 44100;
    private final double MAX_AMPLITUDE = 32767.0;
    private final double TEST_FREQ_BASS = 100.0;
    private final double TEST_FREQ_MID = 1000.0;
    private final double TEST_FREQ_HIGH = 5000.0;

    // <<< A CORREÇÃO FINAL ESTÁ AQUI >>>
    // Os valores padrão devem ser 50 (centro/flat), não 0.
    private int bassLevel = 50, midLevel = 50, trebleLevel = 50;

    private int mainVolume = 50;
    private int panValue = 0; // -100 (Esquerda) a 100 (Direita)

    private int currentTrackIndex = 0;
    private final int[] audioTracks = {R.raw.test_audio, R.raw.skull_music};
    private final String[] trackNames = {"Test Audio Track 1", "Another Audio Track 2"};

    private final IMessageService.Stub binder = new IMessageService.Stub() {
        @Override
        public void sendMessage(String message) throws RemoteException { }

        @Override
        public boolean setBass(int value) throws RemoteException {
            bassLevel = value;
            applyEqualizerSettings();
            return true;
        }

        @Override
        public boolean setMid(int value) throws RemoteException {
            midLevel = value;
            applyEqualizerSettings();
            return true;
        }

        @Override
        public boolean setTreble(int value) throws RemoteException {
            trebleLevel = value;
            applyEqualizerSettings();
            return true;
        }

        @Override
        public boolean setMainVolume(int value) throws RemoteException {
            mainVolume = value;
            applyVolumeAndPan();
            return true;
        }

        @Override
        public boolean setPan(int value) throws RemoteException {
            panValue = value;
            applyVolumeAndPan();
            return true;
        }

        @Override
        public int getValue() throws RemoteException { return myValue; }

        @Override
        public void setValue(int value) throws RemoteException { myValue = value; }

        @Override
        public long getMemoryUsage() throws RemoteException { return getAudioServiceMemoryUsage(); }

        @Override
        public void play() throws RemoteException {
            MessageService.this.stopProcessedAudioNativeInternal();
            MessageService.this.ensurePlayerInitializedIfNeeded();
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
            MessageService.this.stopProcessedAudioNativeInternal();
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.seekTo(0);
                } catch (Exception e) {
                    Log.e(TAG, "Error on stop/prepare: " + e.getMessage());
                    initializeMediaPlayer(audioTracks[currentTrackIndex]);
                }
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
            MessageService.this.ensurePlayerInitializedIfNeeded();
            return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        }

        @Override
        public int getDuration() throws RemoteException {
            MessageService.this.ensurePlayerInitializedIfNeeded();
            return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
        }

        @Override
        public List<String> getAvailableTracks() throws RemoteException {
            return Arrays.asList(trackNames);
        }

        @Override
        public void selectTrack(int trackIndex) throws RemoteException {
            if (trackIndex < 0 || trackIndex >= audioTracks.length) return;

            boolean wasPlaying = isPlaying();
            MessageService.this.stopCurrentPlaybackInternal();
            MessageService.this.stopProcessedAudioNativeInternal();

            currentTrackIndex = trackIndex;
            initializeMediaPlayer(audioTracks[currentTrackIndex]);

            if (wasPlaying) {
                play();
            }
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        }

        @Override
        public int getSelectedTrackIndex() throws RemoteException {
            return currentTrackIndex;
        }

        @Override
        public int triggerNativeHalAudioWrite() throws RemoteException {
            return triggerHalAudioWrite();
        }

        @Override
        public int applyNativeEqualizationTest(int[] gains) throws RemoteException {
            return 0;
        }

        @Override
        public void playProcessedAudioNative(int[] gains, int durationSeconds) throws RemoteException {
            MessageService.this.stopCurrentPlaybackInternal();
            MessageService.this.stopProcessedAudioNativeInternal();
            if (nativeEqualizerProcessor == null) return;
            short[] rawAudioData = generateMultiToneWave(SAMPLE_RATE, TEST_FREQ_BASS, TEST_FREQ_MID, TEST_FREQ_HIGH, MAX_AMPLITUDE, durationSeconds);
            nativeEqualizerProcessor.applyEqualizationNative(rawAudioData, gains);
            int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            try {
                audioTrackNative = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                        .setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(SAMPLE_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                        .setBufferSizeInBytes(Math.max(minBufferSize, rawAudioData.length * Short.BYTES))
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build();
                int state = audioTrackNative.getState();
                Log.d(TAG, "AudioTrack (STREAM) criado. Estado: " + state);
                if (state == AudioTrack.STATE_INITIALIZED) {
                    audioTrackNative.play();
                    int writeResult = audioTrackNative.write(rawAudioData, 0, rawAudioData.length);
                    if (writeResult < 0) Log.e(TAG, "Erro ao escrever dados para AudioTrack (STREAM): " + writeResult);
                    else Log.d(TAG, "Reprodução C++ (STREAM) iniciada com sucesso.");
                } else {
                    Log.e(TAG, "AudioTrack (STREAM) não inicializado. Estado: " + state);
                    audioTrackNative.release();
                    audioTrackNative = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro fatal ao criar AudioTrack (STREAM): " + e.getMessage(), e);
                if (audioTrackNative != null) audioTrackNative.release();
                audioTrackNative = null;
            }
        }

        @Override
        public void stopProcessedAudioNative() throws RemoteException {
            MessageService.this.stopProcessedAudioNativeInternal();
        }
    };

    private void ensurePlayerInitializedIfNeeded() {
        if (mediaPlayer == null) {
            Log.d(TAG, "ensurePlayerInitializedIfNeeded: criando MediaPlayer para trackIndex=" + currentTrackIndex);
            initializeMediaPlayer(audioTracks[currentTrackIndex]);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        nativeEqualizerProcessor = new NativeEqualizerProcessor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAllAudio();
    }

    private void stopAllAudio() {
        stopCurrentPlaybackInternal();
        stopProcessedAudioNativeInternal();
    }

    private void stopProcessedAudioNativeInternal() {
        if (audioTrackNative != null) {
            if (audioTrackNative.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) audioTrackNative.stop();
            audioTrackNative.release();
            audioTrackNative = null;
        }
    }

    private void stopCurrentPlaybackInternal() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            if (equalizer != null) {
                equalizer.release();
                equalizer = null;
            }
        }
    }

    private void initializeMediaPlayer(int audioResId) {
        stopCurrentPlaybackInternal();
        mediaPlayer = MediaPlayer.create(this, audioResId);
        if (mediaPlayer == null) return;
        mediaPlayer.setLooping(true);
        try {
            equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);
            applyEqualizerSettings();
            applyVolumeAndPan();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar Equalizer: " + e.getMessage());
            equalizer = null;
        }
    }

    private void applyVolumeAndPan() {
        if (mediaPlayer == null) return;
        float baseVolume = mainVolume / 100.0f;
        float leftVolume;
        float rightVolume;
        if (panValue > 0) {
            rightVolume = baseVolume;
            float leftMultiplier = 1.0f - (panValue / 100.0f);
            leftVolume = baseVolume * leftMultiplier;
        } else if (panValue < 0) {
            leftVolume = baseVolume;
            float rightMultiplier = 1.0f + (panValue / 100.0f);
            rightVolume = baseVolume * rightMultiplier;
        } else {
            leftVolume = baseVolume;
            rightVolume = baseVolume;
        }
        Log.d(TAG, "Aplicando Volume/Pan: Base=" + baseVolume + ", Pan=" + panValue + ", Final L=" + leftVolume + ", Final R=" + rightVolume);
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    private short[] generateMultiToneWave(int sampleRate, double freqBass, double freqMid, double freqHigh, double amplitude, int durationSeconds) {
        int numSamples = sampleRate * durationSeconds;
        short[] audioData = new short[numSamples];
        double individualAmplitude = amplitude / 3.0;
        for (int i = 0; i < numSamples; i++) {
            double time = (double) i / sampleRate;
            double sample = individualAmplitude * (Math.sin(2 * Math.PI * freqBass * time) + Math.sin(2 * Math.PI * freqMid * time) + Math.sin(2 * Math.PI * freqHigh * time));
            audioData[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample));
        }
        return audioData;
    }

    private void applyEqualizerSettings() {
        if (equalizer == null) {
            Log.w(TAG, "Equalizer é null em applyEqualizerSettings.");
            return;
        }
        short numberOfBands = equalizer.getNumberOfBands();
        if (numberOfBands > 0) {
            short level = (short) ((bassLevel - 50) * 30);
            try {
                equalizer.setBandLevel((short) 0, level);
                Log.d(TAG, "Set Bass (Band 0) to " + level + "mB (user: " + bassLevel + ")");
            } catch (Exception e) {
                Log.e(TAG, "Error setting Bass band level: " + e.getMessage());
            }
        }
        if (numberOfBands > 2) {
            short level = (short) ((midLevel - 50) * 30);
            try {
                equalizer.setBandLevel((short) 2, level);
                Log.d(TAG, "Set Mid (Band 2) to " + level + "mB (user: " + midLevel + ")");
            } catch (Exception e) {
                Log.e(TAG, "Error setting Mid band level: " + e.getMessage());
            }
        }
        if (numberOfBands > 0) {
            short level = (short) ((trebleLevel - 50) * 30);
            try {
                equalizer.setBandLevel((short) (numberOfBands - 1), level);
                Log.d(TAG, "Set Treble (Band " + (numberOfBands - 1) + ") to " + level + "mB (user: " + trebleLevel + ")");
            } catch (Exception e) {
                Log.e(TAG, "Error setting Treble band level: " + e.getMessage());
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.circle_users_adapter).setContentTitle("Emixer App").setContentText("Ajustando áudio em segundo plano").setPriority(NotificationCompat.PRIORITY_LOW).build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Emixer Service Channel", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
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
