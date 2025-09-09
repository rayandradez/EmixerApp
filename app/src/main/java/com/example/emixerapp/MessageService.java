package com.example.emixerapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.os.Debug;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.example.emixerapp.equalization.NativeEqualizerProcessor;
import com.example.emixerapp.notification.NotificationModule;
import com.example.emixerapp.playback.MediaPlayerPlaybackController;
import com.example.emixerapp.playback.PlaybackController;
import com.reaj.emixer.IMessageService;
import com.reaj.emixer.R;

import java.util.Arrays;
import java.util.List;

public class MessageService extends Service {

    private static final String TAG = "MessageService";
    private NotificationModule notificationModule;
    private NotificationManager notificationManager;

    // --- Módulos de Lógica ---
    private PlaybackController playbackController;
    private NativeEqualizerProcessor nativeEqualizerProcessor;
    private AudioTrack audioTrackNative; // Para áudio C++
    private Equalizer equalizer; // Equalizador para o MediaPlayer

    // --- Estado do Serviço ---
    private int bassLevel = 50, midLevel = 50, trebleLevel = 50;
    private int mainVolume = 50;
    private int panValue = 0;
    private int currentTrackIndex = 0;
    private final int[] audioTracks = {R.raw.test_audio, R.raw.skull_music};
    private final String[] trackNames = {"Test Audio Track 1", "Another Audio Track 2"};

    // --- Lógica de Áudio Nativo (C++) ---
    static { try { System.loadLibrary("emixer"); } catch (UnsatisfiedLinkError e) { Log.e(TAG, "Falha ao carregar lib nativa", e); } }
    public native int triggerHalAudioWrite();
    private final int SAMPLE_RATE = 44100;
    private final double MAX_AMPLITUDE = 32767.0;

    private final IMessageService.Stub binder = new IMessageService.Stub() {
        // --- Comandos de Reprodução (delegados ao PlaybackController) ---
        @Override
        public void play() throws RemoteException {
            stopProcessedAudioNativeInternal(); // Para o áudio C++ se estiver tocando
            if (playbackController == null) ensurePlayerInitializedIfNeeded();
            playbackController.play();
            Log.d(TAG, "Comando Play delegado.");
            updateNotification("Tocando");
        }
        @Override
        public void pause() throws RemoteException {
            if (playbackController != null) playbackController.pause();
            Log.d(TAG, "Comando Pause delegado.");
            updateNotification("Pausado");
        }
        @Override
        public void stop() throws RemoteException {
            stopProcessedAudioNativeInternal();
            if (playbackController != null) playbackController.stop();
            Log.d(TAG, "Comando Stop delegado.");
            updateNotification("Parado");
        }
        @Override
        public void seekTo(int positionMs) throws RemoteException {
            if (playbackController != null) playbackController.seekTo(positionMs);
        }
        @Override
        public boolean isPlaying() throws RemoteException {
            return playbackController != null && playbackController.isPlaying();
        }
        @Override
        public int getCurrentPosition() throws RemoteException {
            return playbackController != null ? playbackController.getCurrentPosition() : 0;
        }
        @Override
        public int getDuration() throws RemoteException {
            return playbackController != null ? playbackController.getDuration() : 0;
        }
        @Override
        public List<String> getAvailableTracks() throws RemoteException {
            return Arrays.asList(trackNames);
        }
        @Override
        public int getSelectedTrackIndex() throws RemoteException {
            return currentTrackIndex;
        }
        @Override
        public void selectTrack(int trackIndex) throws RemoteException {
            if (trackIndex < 0 || trackIndex >= audioTracks.length) return;
            boolean wasPlaying = isPlaying();
            stopAllAudio(); // Para tudo antes de trocar
            currentTrackIndex = trackIndex;
            initializeMediaPlayer(audioTracks[currentTrackIndex]);
            if (wasPlaying) {
                play();
            }
        }

        // --- Comandos de Configuração de Áudio (afetam o estado do serviço) ---
        @Override public boolean setBass(int value) throws RemoteException { bassLevel = value; applyEqualizerSettings(); return true; }
        @Override public boolean setMid(int value) throws RemoteException { midLevel = value; applyEqualizerSettings(); return true; }
        @Override public boolean setTreble(int value) throws RemoteException { trebleLevel = value; applyEqualizerSettings(); return true; }
        @Override public boolean setMainVolume(int value) throws RemoteException { mainVolume = value; if (playbackController != null) playbackController.applyVolumeAndPan(mainVolume, panValue); return true; }
        @Override public boolean setPan(int value) throws RemoteException { panValue = value; if (playbackController != null) playbackController.applyVolumeAndPan(mainVolume, panValue); return true; }

        // --- Comandos de Áudio Nativo (C++) ---
        @Override
        public void playProcessedAudioNative(int[] gains, int durationSeconds) throws RemoteException {
            stopAllAudio();
            if (nativeEqualizerProcessor == null) return;
            short[] rawAudioData = generateMultiToneWave(SAMPLE_RATE, 100.0, 1000.0, 5000.0, MAX_AMPLITUDE, durationSeconds);
            nativeEqualizerProcessor.applyEqualizationNative(rawAudioData, gains);
            int bufferSize = rawAudioData.length * Short.BYTES;
            try {
                audioTrackNative = new AudioTrack.Builder().setAudioAttributes(new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_MEDIA).setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new android.media.AudioFormat.Builder().setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT).setSampleRate(SAMPLE_RATE).setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(bufferSize).setTransferMode(AudioTrack.MODE_STATIC).build();
                int writeResult = audioTrackNative.write(rawAudioData, 0, rawAudioData.length);
                if (writeResult >= 0) { audioTrackNative.play(); Log.d(TAG, "Reprodução C++ (STATIC) iniciada."); } else { Log.e(TAG, "Falha ao escrever no AudioTrack. Erro: " + writeResult); audioTrackNative.release(); audioTrackNative = null; }
            } catch (Exception e) { Log.e(TAG, "Erro ao criar AudioTrack", e); if (audioTrackNative != null) audioTrackNative.release(); audioTrackNative = null; }
        }
        @Override public void stopProcessedAudioNative() throws RemoteException { stopProcessedAudioNativeInternal(); }
        @Override public int triggerNativeHalAudioWrite() throws RemoteException { return triggerHalAudioWrite(); }

        // --- Comandos de Serviço (não relacionados ao áudio) ---
        @Override public long getMemoryUsage() throws RemoteException { return getAudioServiceMemoryUsage(); }
        @Override public int getValue() throws RemoteException { return 0; }
        @Override public void setValue(int value) throws RemoteException { }
        @Override public void sendMessage(String message) throws RemoteException { }
        @Override public int applyNativeEqualizationTest(int[] gains) throws RemoteException { return 0; }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        notificationModule = new NotificationModule(this, "emixer_channel");
        notificationModule.createNotificationChannel();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nativeEqualizerProcessor = new NativeEqualizerProcessor();
        // O PlaybackController é criado sob demanda em ensurePlayerInitializedIfNeeded()
    }

    @Override public IBinder onBind(Intent intent) { return binder; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = notificationModule.createNotification("Emixer App", "Serviço de áudio ativo");
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAllAudio();
    }

    private void ensurePlayerInitializedIfNeeded() {
        if (playbackController == null) {
            Log.d(TAG, "PlaybackController é null. Inicializando agora.");
            initializeMediaPlayer(audioTracks[currentTrackIndex]);
        }
    }

    private void initializeMediaPlayer(int audioResId) {
        playbackController = new MediaPlayerPlaybackController(this);
        playbackController.selectTrack(audioResId);

        // Após preparar a faixa, podemos obter o ID da sessão para o Equalizer
        int sessionId = playbackController.getAudioSessionId();
        if (sessionId != 0) {
            try {
                if (equalizer != null) equalizer.release();
                equalizer = new Equalizer(0, sessionId);
                equalizer.setEnabled(true);
                applyEqualizerSettings();
                playbackController.applyVolumeAndPan(mainVolume, panValue);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao inicializar Equalizer: " + e.getMessage());
                equalizer = null;
            }
        }
    }

    private void stopAllAudio() {
        if (playbackController != null) {
            playbackController.release();
            playbackController = null;
        }
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
        stopProcessedAudioNativeInternal();
    }

    private void stopProcessedAudioNativeInternal() {
        if (audioTrackNative != null) {
            if (audioTrackNative.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) audioTrackNative.stop();
            audioTrackNative.release();
            audioTrackNative = null;
        }
    }

    private void applyEqualizerSettings() {
        if (equalizer == null) return;
        short numberOfBands = equalizer.getNumberOfBands();
        if (numberOfBands > 0) equalizer.setBandLevel((short) 0, (short) ((bassLevel - 50) * 30));
        if (numberOfBands > 2) equalizer.setBandLevel((short) 2, (short) ((midLevel - 50) * 30));
        if (numberOfBands > 0) equalizer.setBandLevel((short) (numberOfBands - 1), (short) ((trebleLevel - 50) * 30));
    }

    private void updateNotification(String text) {
        Notification updatedNotification = notificationModule.createNotification("Emixer App", text);
        notificationManager.notify(1, updatedNotification);
    }

    private short[] generateMultiToneWave(int sampleRate, double f1, double f2, double f3, double amp, int dur) {
        int numSamples = sampleRate * dur; short[] data = new short[numSamples]; double individualAmp = amp / 3.0;
        for (int i = 0; i < numSamples; i++) { double time = (double) i / sampleRate; double sample = individualAmp * (Math.sin(2 * Math.PI * f1 * time) + Math.sin(2 * Math.PI * f2 * time) + Math.sin(2 * Math.PI * f3 * time)); data[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample)); }
        return data;
    }

    private long getAudioServiceMemoryUsage() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) { Debug.MemoryInfo[] memInfo = am.getProcessMemoryInfo(new int[]{Process.myPid()}); if (memInfo != null && memInfo.length > 0) { return memInfo[0].getTotalPss(); } }
        return 0;
    }
}
