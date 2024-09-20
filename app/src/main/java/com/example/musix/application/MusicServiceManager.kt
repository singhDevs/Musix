package com.example.musix.application

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.musix.services.MusicService

//object MusicServiceManager {
//    @SuppressLint("StaticFieldLeak")
//    private var musicService: MusicService? = null
//    private val serviceConnection: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            Log.d("TAG", "onServiceConnected called.")
//            val binder = service as MusicService.LocalBinder
//            musicService = binder.getServiceInstance()
//        }
//
//        override fun onServiceDisconnected(name: ComponentName) {
//            Log.d("TAG", "onServiceDisconnected called")
//        }
//    }
//    fun getMusicServiceInstance(): MusicService{
//        if(musicService == null){
//
//        }
//        return musicService!!
//    }
//}