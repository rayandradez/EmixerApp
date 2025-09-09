// IMessageService.aidl
package com.reaj.emixer;

import java.util.List;

interface IMessageService {
    void sendMessage(String message);
    boolean setBass(int value);
    boolean setMid(int value);
    boolean setTreble(int value);
    boolean setMainVolume(int value);
    boolean setPan(int value);

    int getValue();
    void setValue(int value);
    long getMemoryUsage();

    void play();
    void pause();
    void stop();
    void seekTo(int positionMs);
    int getCurrentPosition();
    int getDuration();
    List<String> getAvailableTracks();
    void selectTrack(int trackIndex);
    boolean isPlaying();

    // Nativo
    int triggerNativeHalAudioWrite();
    int applyNativeEqualizationTest(in int[] gains);
    void playProcessedAudioNative(in int[] gains, int durationSeconds);
    void stopProcessedAudioNative();

    // <<< NOVO MÉTODO PARA SINCRONIZAR A UI >>>
    int getSelectedTrackIndex();
}
