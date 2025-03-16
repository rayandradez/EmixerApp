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
    void setValue(int value); // Adicione este método
   long getMemoryUsage();
}