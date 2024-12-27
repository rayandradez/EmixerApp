package com.example.emixerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class AirplaneModeBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val isAirplaneModeOn =  intent?.getBooleanExtra("state", false) ?: return
        if (isAirplaneModeOn){
            Toast.makeText(context, "Airplane mode ON, won't sync across devices", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Airplane mode OFF, syncing across devices", Toast.LENGTH_LONG).show()
        }
    }
}