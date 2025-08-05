// IMessageService.aidl
package com.reaj.emixer;

// Declare any non-default types here with import statements

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

   // Novas funções para controle de reprodução
     void play();
     void pause();
     void stop();
     void seekTo(int positionMs); // Define a posição de reprodução em milissegundos
     int getCurrentPosition();    // Retorna a posição atual em milissegundos
     int getDuration();           // Retorna a duração total da faixa em milissegundos
     List<String> getAvailableTracks(); // Retorna uma lista de nomes das faixas disponíveis
     void selectTrack(int trackIndex); // Seleciona uma faixa pelo índice
     boolean isPlaying();
}