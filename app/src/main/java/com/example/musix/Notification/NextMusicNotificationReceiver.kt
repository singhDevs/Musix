package com.example.musix.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import com.example.musix.application.RunningApp
import com.example.musix.services.MusicService

class NextMusicNotificationReceiver: BroadcastReceiver() {
    private var runningApp: RunningApp? = null
    private var musicService: MusicService? = null
    private var serviceConnection: ServiceConnection? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TAG", "Next Btn on Notification clicked")

        runningApp = context?.applicationContext as RunningApp
        if (runningApp != null) {
            Log.d("TAG", "initializing music service in Music Player & Service Connection")
            musicService = runningApp?.getMusicService()
            serviceConnection = runningApp?.getServiceConnection()
        } else {
            Log.d("TAG", "Running App is NULL")
        }

        musicService?.playNext()
    }
}
