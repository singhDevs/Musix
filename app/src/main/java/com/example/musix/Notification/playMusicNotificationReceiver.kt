package com.example.musix.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PlayMusicNotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TAG", "Play Btn on Notification clicked")
    }

}
