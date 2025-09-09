package com.example.emixerapp.playback;

/**
 * Define o contrato para um controlador de reprodução de áudio.
 * Qualquer classe que controle a reprodução (seja com MediaPlayer, AudioTrack, etc.)
 * deve implementar esta interface.
 */
public interface PlaybackController {

    /**
     * Seleciona e prepara uma nova faixa de áudio para reprodução.
     * @param audioResId O ID do recurso de áudio.
     */
    void selectTrack(int audioResId);

    /** Inicia ou resume a reprodução. */
    void play();

    /** Pausa a reprodução. */
    void pause();

    /** Para a reprodução e reseta para o início. */
    void stop();

    /**
     * Busca uma posição específica no áudio.
     * @param positionMs A posição em milissegundos.
     */
    void seekTo(int positionMs);

    /**
     * Aplica as configurações de volume e pan (balanço).
     * @param mainVolume O volume geral (0-100).
     * @param panValue O valor do pan (-100 a 100).
     */
    void applyVolumeAndPan(int mainVolume, int panValue);

    /** Verifica se o áudio está tocando. */
    boolean isPlaying();

    /** Obtém a duração total da faixa atual em milissegundos. */
    int getDuration();

    /** Obtém a posição atual da reprodução em milissegundos. */
    int getCurrentPosition();

    /**
     * Obtém o ID da sessão de áudio, necessário para o Equalizer.
     * @return O ID da sessão de áudio, ou 0 se não estiver disponível.
     */
    int getAudioSessionId();

    /** Libera todos os recursos usados pelo controlador. */
    void release();
}
