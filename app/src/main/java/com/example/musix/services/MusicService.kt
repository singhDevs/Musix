package com.example.musix.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musix.Notification.MusicPlayerNotificationService
import com.example.musix.R
import com.example.musix.models.Song
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import java.io.Serializable

class MusicService(): Service()
{
    companion object{
        const val PLAYING_MUSIC: Int = 1
        const val PAUSED_MUSIC: Int = 2
        const val NOT_REPEATED = 0
        const val REPEAT = 1
        const val REPEAT_ONE = 2
        const val NO_SHUFFLE = 0
        const val SHUFFLE = 1
        val song = Song("", "", "", "", "", 0, "", 0)
        const val SONG_CHANGED = "com.example.musix.models.Song"
        const val PLAYER_PLAYING = "player_ready"
    }
    private var isServiceRunning = false
    private var sameSong = false
    var musicStatus: Int = PLAYING_MUSIC
    var repeatStatus: Int = NOT_REPEATED
    var shuffleStatus: Int = NO_SHUFFLE
    val context: Context = this
    private var playlistName: String = "Default"
    private lateinit var songList: List<Song?>
    private var songPosition: Int = 0
    private lateinit var player: ExoPlayer
    private val mBinder: IBinder = LocalBinder(this)
    private var songURL: String = ""
    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val musicPlayerNotificationService = songList[songPosition]?.let { MusicPlayerNotificationService(applicationContext, it) }
        val notification = musicPlayerNotificationService!!.getNotification()
        notificationManager.notify(1, notification)
        startForeground(1, notification)
        return mBinder
    }

    class LocalBinder(private val musicService: MusicService): Binder(){
        fun getServiceInstance(): MusicService{
            return musicService
        }
    }

    override fun onCreate() {
        super.onCreate()
        player = SimpleExoPlayer.Builder(context).build()
        songList = listOf(song)
        isServiceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            Log.d("TAG", "inside INTENT");
            playlistName = intent.getStringExtra("playlistName").toString() ?: ""
            songList = (intent.getSerializableExtra("songList") as? List<Song>) ?: listOf(song)
            songPosition = intent.getIntExtra("songPosition", 0)
            songURL = intent.getStringExtra("songUrl").toString()
        }
        else{
            Log.d("TAG", "Intent is NULL")
        }

        if(songList[0] != song){
            songList[songPosition]?.let { initializePlayer(it.id) }
            if(!sameSong) songList[songPosition]?.let { initializePlayer(it.id) }
        }
        return START_STICKY
    }

    fun fetchData(songList: List<Song?>, songPosition: Int, playlistName: String){
        if(this@MusicService.songList != songList) Log.d("TAG", "songList are not same!!")
        //TODO: add equality check on songList as well
        //TODO: add equality check on songList as well
        if(this@MusicService.songList[0] != songList[0] && this@MusicService.songPosition == songPosition && this@MusicService.playlistName == playlistName){
            Log.d("TAG", "sameSong set TRUE")
            sameSong = true
        }
        else{
            Log.d("TAG", "\n\nsameSong set FALSE")
            sameSong = false
            this@MusicService.songList = songList
            this@MusicService.songPosition = songPosition
            this@MusicService.playlistName = playlistName
            resetPlayer()
            songList[songPosition]?.let { initializePlayer(it.id) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun initializePlayer(songUri: String) {
        Log.d("TAG", "Initializing Player")
        val mediaItem = MediaItem.fromUri(songUri)

        player.setMediaItem(mediaItem)
        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == Player.STATE_ENDED){
                    playNext()
                    notifySongChanged()
                }
//                if(playbackState == Player.STATE_READY){
//                    playerReady();
//                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if(isPlaying){
                    Log.d("TAG", "isPlaying True, calling playerPlaying()")
                    playerPlaying()
                }
                else{
                    Log.d("TAG", "isPlaying False")
                }
            }

            private fun playerPlaying() {
                val intent = Intent(PLAYER_PLAYING)
                Log.d("TAG", "Player Ready Broadcast sent")
                intent.putExtra("songTitle", songList[songPosition]!!.title)
                intent.putExtra("songArtist", songList[songPosition]!!.artist)
                intent.putExtra("songBanner", songList[songPosition]!!.banner)
                intent.putExtra("songList",songList as Serializable)
                intent.putExtra("songPosition", songPosition)
                intent.putExtra("playlistName", playlistName)
                LocalBroadcastManager.getInstance(this@MusicService).sendBroadcast(intent)
            }

            private fun notifySongChanged() {
                val intent = Intent(SONG_CHANGED)
                Log.d("TAG", "Song Changed Broadcast sent, songPos: " + songPosition)
                intent.putExtra("songPosition", songPosition)
                LocalBroadcastManager.getInstance(this@MusicService).sendBroadcast(intent)
            }
        })
        player.prepare()
        player.play()
    }

    fun playMusic(){
        player.playWhenReady = true
        player.play()
        musicStatus = PLAYING_MUSIC
        Log.d("TAG", "PLaying music from Service")
    }

    fun pauseMusic(){
        player.pause()
        Log.d("TAG", "Music Paused in Service")
    }

    fun playNext(){
//        initializePlayer(songUri)

        if (repeatStatus == REPEAT) {
            if (shuffleStatus == SHUFFLE) {
                resetPlayer()
                var randomPosition = songPosition
                while (songPosition == randomPosition) {
                    randomPosition = (Math.random() * songList.size + 0).toInt()
                }
                songPosition = randomPosition
                songList[songPosition]?.let { initializePlayer(it.id) }
//                    setUpUI(songList[songPosition])
            } else {
                if (songPosition < songList.size - 1) {
                    resetPlayer()
                    songList[++songPosition]?.let { initializePlayer(it.id) }
//                        setUpUI(songList[songPosition])
                } else {
                    resetPlayer()
                    songList[0]?.let { initializePlayer(it.id) }
//                        setUpUI(songList[0])
                    songPosition = 0
                }
            }
        } else if (repeatStatus == REPEAT_ONE) {
            resetPlayer()
            playMusic()
        } else {
            if (shuffleStatus == SHUFFLE) {
                resetPlayer()
                var randomPosition = songPosition
                while (songPosition == randomPosition) {
                    randomPosition = (Math.random() * songList.size + 0).toInt()
                }
                songPosition = randomPosition
                songList[songPosition]?.let { initializePlayer(it.id) }
//                    setUpUI(songList[songPosition])
            } else {
                if (songPosition < songList.size - 1) {
                    resetPlayer()
                    songList[++songPosition]?.let { initializePlayer(it.id) }
//                        setUpUI(songList[songPosition])
                } else {
                    resetPlayer()
                }
            }
        }
    }

    fun playPrev(){
//        initializePlayer(songUri)

        if (player.currentPosition / 1000 <= 2) {
            if (songPosition == 0) {
                resetPlayer()
                playMusic()
            } else {
                resetPlayer()
                songList[--songPosition]?.let { initializePlayer(it.id) }
//                    setUpUI(songList[songPosition])
            }
        } else {
            resetPlayer()
            playMusic()
        }
    }

    private fun resetPlayer(){
        musicStatus = PAUSED_MUSIC
        player.pause()
        player.seekTo(0)
    }

    fun getCurrentPosition() = player.currentPosition

    fun playerSeekTo(position: Long){
        player.seekTo(position)
    }

    fun releasePlayer(){
        player.stop()
        player.release()
    }
}