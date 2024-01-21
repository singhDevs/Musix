package com.example.musix.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.musix.services.MusicService
import com.example.musix.services.MusicService.LocalBinder

class RunningApp: Application() {
    private var musicService: MusicService? = null
    private var serviceConnection: ServiceConnection? = null

    override fun onCreate() {
        super.onCreate()
        setServiceConnection()
        createNotificationChannel()
        startMusicService()
        if(musicService == null) Log.d("TAG", "in application class, music service is NULL")
    }

    private fun startMusicService(){
        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        startService(serviceIntent)
    }

    fun getMusicService(): MusicService?{
        return musicService
    }

    private fun setMusicService(musicService: MusicService){
        Log.d("TAG", "Setting musicService: $musicService")
        this@RunningApp.musicService = musicService
        Log.d("TAG", "musicService is now: ${this@RunningApp.musicService}")
    }

    fun getServiceConnection(): ServiceConnection?{
        return serviceConnection
    }

    private fun setServiceConnection(){
        serviceConnection = object: ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d("TAG", "onServiceConnected called")
                val binder = service as LocalBinder
                if(binder.getServiceInstance() == null) Log.d("TAG", "binder is NULL")
                setMusicService(binder.getServiceInstance())
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d("TAG", "onServiceDisconnected called")
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopService(Intent(this, MusicService::class.java))
        unbindService(serviceConnection!!)
        serviceConnection = null
        musicService = null
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                MUSIC_PLAYER_CHANNEL_ID,
                "Music Player Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val MUSIC_PLAYER_CHANNEL_ID = "music_player_channel"
    }
}