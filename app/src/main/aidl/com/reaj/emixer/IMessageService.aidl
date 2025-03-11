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
}