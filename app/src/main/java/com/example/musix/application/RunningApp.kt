package com.example.musix.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.room.Room
import com.example.musix.database.LikedSongsDatabase
import com.example.musix.models.Song
import com.example.musix.services.MusicService
import com.example.musix.services.MusicService.LocalBinder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RunningApp: Application() {
    private var musicService: MusicService? = null
    private var serviceConnection: ServiceConnection? = null
    lateinit var database: LikedSongsDatabase
    lateinit var likedSongsRepository: LikedSongsRepository

    override fun onCreate() {
        super.onCreate()
        setServiceConnection()

        GlobalScope.launch{
            Log.d("TAG", "inside coroutine, calling imp functions...")
            initDatabase()
            likedSongsRepository.handleLikedSongs()
            createNotificationChannel()
            startMusicService()
            if(musicService == null) Log.d("TAG", "in application class, music service is NULL")
        }
    }

    private suspend fun initDatabase() {
        database = Room.databaseBuilder(
            applicationContext,
            LikedSongsDatabase::class.java,
            "my_database"
        ).build()
        likedSongsRepository = LikedSongsRepository(database)
        database.songDao().deleteAllLikedSongs()
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
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val MUSIC_PLAYER_CHANNEL_ID = "music_player_channel"
    }

    class LikedSongsRepository(private val database: LikedSongsDatabase) {
        suspend fun handleLikedSongs() {
            Log.d("TAG", "starting to handle liked songs...")
            val songKeysList = fetchLikedSongKeys()
            Log.d("TAG", "Liked Song Keys fetched, now fetching Objects...")
            val likedSongsList = fetchSongsFromKeys(songKeysList)
            Log.d("TAG", "Liked Song Objects fetched, now populating ROOM DB...")
            addLikedSongsToRoom(likedSongsList)
            Log.d("TAG", "Populated ROOM DB successfully!")
        }

        private suspend fun fetchLikedSongKeys(): List<String> = suspendCoroutine { continuation ->
            val reference = FirebaseDatabase.getInstance().getReference()
                .child("playlist")
                .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .child("liked")
                .child("songs")

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val songKeysList = snapshot.children.map { it.key ?: "" }
                    continuation.resume(songKeysList)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }

        private suspend fun fetchSongsFromKeys(songKeysList: List<String>): List<Song> = suspendCoroutine { continuation ->
            val songsList = mutableListOf<Song>()

            FirebaseDatabase.getInstance().getReference().child("songs")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (snapshot1 in snapshot.children) {
                            val song = snapshot1.getValue(Song::class.java)
                            if (song != null && song.key in songKeysList) {
                                songsList.add(song)
                            }
                        }
                        continuation.resume(songsList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }

        suspend fun addLikedSongsToRoom(likedSongsList: List<Song>) {
            withContext(Dispatchers.IO) {
                for(song in likedSongsList){
                    database.songDao().addSong(song)
                }
            }
        }
    }


}