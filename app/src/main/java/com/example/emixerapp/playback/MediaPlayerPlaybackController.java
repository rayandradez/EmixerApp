package com.example.emixerapp.playback;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Implementação do PlaybackController que usa a classe MediaPlayer do Android.
 */
public class MediaPlayerPlaybackController implements PlaybackController {

    private static final String TAG = "MediaPlayerPlayback";
    private MediaPlayer mediaPlayer;
    private final Context context;

    public MediaPlayerPlaybackController(Context context) {
        this.context = context;
    }

    @Override
    public void selectTrack(int audioResId) {
        // Libera qualquer instância anterior antes de criar uma nova.
        release();
        try {
            mediaPlayer = MediaPlayer.create(context, audioResId);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
            } else {
                Log.e(TAG, "MediaPlayer.create falhou para o resource ID: " + audioResId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao selecionar a faixa: " + audioResId, e);
            mediaPlayer = null;
        }
    }

    @Override
    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            try {
                // Após o stop(), o player precisa ser preparado novamente.
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                Log.e(TAG, "Erro durante o stop/prepare do MediaPlayer", e);
                // Em caso de erro grave, melhor não reutilizar.
                release();
            }
        }
    }

    @Override
    public void seekTo(int positionMs) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(positionMs);
        }
    }

    @Override
    public void applyVolumeAndPan(int mainVolume, int panValue) {
        if (mediaPlayer == null) return;

        float volume = mainVolume / 100.0f;
        float pan = panValue / 100.0f;
        float leftVolume;
        float rightVolume;

        if (pan > 0) { // Pan para a direita
            rightVolume = volume;
            leftVolume = volume * (1.0f - pan);
        } else { // Pan para a esquerda ou centro
            leftVolume = volume;
            rightVolume = volume * (1.0f + pan);
        }
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer != null ? mediaPlayer.getAudioSessionId() : 0;
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
