@file:OptIn(UnstableApi::class) package com.example.musix.services

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.MediaController.releaseFuture
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import com.example.musix.MediaPlayback
import com.example.musix.MediaPlayback.mediaSession
import com.example.musix.MediaPlayback.player
//import com.example.musix.notification.MusicPlayerNotificationService
import com.example.musix.R
//import com.example.musix.activities.NewMusicPlayer.mediaController
import com.example.musix.models.Song
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "Musix"

class MusicService: MediaSessionService()
{
    companion object{
        const val NOT_STARTED: Int = 0
        const val PLAYING_MUSIC: Int = 1
        const val PAUSED_MUSIC: Int = 2
        const val NOT_REPEATED = 0
        const val REPEAT = 1
        const val REPEAT_ONE = 2
        const val NO_SHUFFLE = 0
        const val SHUFFLE = 1

        const val SAVE_TO_LIKED = "save_to_liked"
        const val REMOVE_FROM_LIKED = "remove_from_liked"
        const val PLAY_NEXT = "play_next"
        const val PLAY_PREV = "play_prev"

        val song = Song("", "", "", "", "", 0, "", 0, "", "")
        const val SONG_CHANGED = "com.example.musix.models.Song"
        const val PLAYER_PLAYING = "player_ready"
        fun mediaItemBuilder(song: Song){
            Log.d(TAG, "mediaItemBuilder called for: ${song.title}")
            val mediaItem = MediaItem.Builder()
                .setUri(song.id)
                .setMediaId(song.key)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(song.banner.toUri())
                        .setDurationMs(song.durationInSeconds*1000L)
                        .build()
                ).build()
            MediaPlayback.mediaController?.setMediaItem(mediaItem)
        }
    }

    private var isServiceRunning = false
    private var sameSong = false

    val context: Context = this
    private var playlistName: String = "Default"
    private lateinit var songList: List<Song?>
    private var songPosition: Int = 0
    private lateinit var controllerFuture : ListenableFuture<MediaController>
    private var songURL: String = ""
    private lateinit var notificationManager: NotificationManager
    private lateinit var sessionToken: SessionToken

    private val likeButton = CommandButton.Builder()
        .setIconResId(R.drawable.heart_outline)
        .setDisplayName("Add to Liked songs")
        .setSessionCommand(SessionCommand(SAVE_TO_LIKED, Bundle().apply { putString("songKey", mediaSession?.player?.currentMediaItem?.mediaId.toString()) }))
        .build()

    private val unlikeButton = CommandButton.Builder()
        .setIconResId(R.drawable.heart_filled)
        .setDisplayName("Remove from Liked songs")
        .setSessionCommand(SessionCommand(REMOVE_FROM_LIKED, Bundle()))
        .build()

    private val playNext = CommandButton.Builder()
        .setIconResId(R.drawable.ic_next)
        .setDisplayName("Play Next")
        .setSessionCommand(SessionCommand(PLAY_NEXT, Bundle()))
        .build()

    private val playPrev = CommandButton.Builder()
        .setIconResId(R.drawable.ic_previous)
        .setDisplayName("Play Next")
        .setSessionCommand(SessionCommand(PLAY_PREV, Bundle()))
        .build()

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    @OptIn(UnstableApi::class) override fun onCreate() {
        super.onCreate()

//        val forwardingPlayer = object : ForwardingPlayer(player) {
//            override fun seekToNext() {
//                super.seekToNext()
//            }
//
//            override fun seekForward() {
//                super.seekForward()
//            }
//        }

        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player!!)
            .setCallback(MediaCallback())
            .setCustomLayout(ImmutableList.of(playPrev, playNext))
            .build()

        Log.d(TAG, "Setting up Media Controller...")
        sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture =
            MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            MediaPlayback.mediaController = controllerFuture.get()
            MediaPlayback.mediaControllerCallback?.onMediaControllerAvailable()
        }, ContextCompat.getMainExecutor(this))

//        songList = listOf(song)
//        isServiceRunning = true
    }

    private inner class MediaCallback : MediaSession.Callback{
        @OptIn(UnstableApi::class) override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            // Set available player and session commands.
            val songId: Bundle = Bundle().apply {
                putString("song_id", session.player.currentMediaItem?.mediaId)
            }
            val sessionCommands = SessionCommands.Builder()
                .add(SessionCommand(SAVE_TO_LIKED, songId))
                .build()
            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == SAVE_TO_LIKED) {
                saveToFavorites(session.player.currentMediaItem)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    private fun saveToFavorites(mediaItem: MediaItem?) {
        Log.d(TAG, "Saving to favorites: ${mediaItem?.mediaMetadata?.title}")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
//        FirebaseHandler.addLikedSong(this, playlistName, uid, mediaItem.mediaId, applicationContext as RunningApp)
        mediaSession?.setCustomLayout(ImmutableList.of(unlikeButton))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED) {
            /**
             * Stop the service if not playing, continue playing in the background otherwise.
              */
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            releaseFuture(controllerFuture)
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}


/**
    private val mBinder: IBinder = LocalBinder(this)
        override fun onBind(intent: Intent?): IBinder {
            super.onBind(intent)
            return mBinder
    }
    class LocalBinder(private val musicService: MusicService): Binder(){
        fun getServiceInstance(): MusicService{
            return musicService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
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
            songList[songPosition]?.let { initializePlayer(it) }
            if(!sameSong) songList[songPosition]?.let { initializePlayer(it) }
        }
        return START_STICKY
    }

    fun fetchData(songList: List<Song?>, songPosition: Int, playlistName: String){
        Log.d("TAG", "fetchData called...");
        if(this@MusicService.songList != songList) Log.d("TAG", "songList are not same!!")
        else Log.d("TAG", "songList are same!!")

        if(this@MusicService.songList[this@MusicService.songPosition]?.key != songList[songPosition]?.key){
            Log.d("TAG", "Key NOT SAME, reinitializing player...");
            this@MusicService.songList = songList
            this@MusicService.songPosition = songPosition
            this@MusicService.playlistName = playlistName
            resetPlayer()
            songList[songPosition]?.let { initializePlayer(it) }
        }
        else{
            Log.d("TAG", "Key is SAME, song continues to play...");
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        releasePlayer()
//    }

    private fun initializePlayer(song: Song) {
        Log.d("TAG", "------------------------Initializing Player------------------------")
        Log.d("TAG", "Song Pos: " + songPosition + "\t\tSong: " + songList.get(songPosition)?.title)
        Log.d("TAG", "setting Notification in init player...")
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val musicPlayerNotificationService = songList[songPosition]?.let { MusicPlayerNotificationService(applicationContext, it, R.drawable.ic_pause) }
        val notification = musicPlayerNotificationService!!.getNotification()
//        notificationManager.notify(1, notification)
//        startForeground(1, notification)

//        val mediaItem = MediaItem.fromUri(songUri)

        val mediaItem =
            MediaItem.Builder()
                .setMediaId("media-1")
                .setUri(song.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist(song.artist)
                        .setTitle(song.title)
                        .setArtworkUri(song.banner.toUri())
                        .build()
                )
                .build()

        mediaController?.let {
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }

        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d("TAG", "ALERT:\t\tPlayback State: " + playbackState.toString())
                if(playbackState == Player.STATE_ENDED){
                    Log.d("TAG", "ALERT6: calling playNext()")
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
        })
//        player.prepare()
//        player.play()
        musicStatus = PLAYING_MUSIC
        Log.d("TAG", "------------------------initialize player ends------------------------")
    }

    fun playMusic(){
        player.playWhenReady = true
        player.play()
        musicStatus = PLAYING_MUSIC
        Log.d("TAG", "setting Notification in init player...")
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val musicPlayerNotificationService = songList[songPosition]?.let { MusicPlayerNotificationService(applicationContext, it, R.drawable.ic_pause) }
        val notification = musicPlayerNotificationService!!.getNotification()
//        notificationManager.notify(1, notification)
//        startForeground(1, notification)
        Log.d("TAG", "PLaying music from Service")
    }

    fun notifySongChanged() {
        val intent = Intent(SONG_CHANGED)
        Log.d("TAG", "inside notif, songPos: " + songPosition)
        Log.d("TAG", "Song Changed Broadcast sent, songPos: " + songPosition)
        intent.putExtra("songPosition", songPosition)
        LocalBroadcastManager.getInstance(this@MusicService).sendBroadcast(intent)
    }

    fun pauseMusic(){
        player.pause()
        Log.d("TAG", "setting Notification in init player...")
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val musicPlayerNotificationService = songList[songPosition]?.let { MusicPlayerNotificationService(applicationContext, it, R.drawable.ic_play_arrow) }
        val notification = musicPlayerNotificationService!!.getNotification()
//        notificationManager.notify(1, notification)
//        startForeground(1, notification)
        Log.d("TAG", "Music Paused in Service")
    }

    fun playNext(){
//        initializePlayer(songUri)
        Log.d("TAG", "ALERTSHUFFLE: " + shuffleStatus)
        Log.d("TAG", "REPEATSTATUS: " + repeatStatus)
        if (repeatStatus == REPEAT) {
            if (shuffleStatus == SHUFFLE) {
                resetPlayer()
                var randomPosition = songPosition
                while (songPosition == randomPosition) {
                    randomPosition = (Math.random() * songList.size + 0).toInt()
                }
                songPosition = randomPosition

                Log.d("TAG", "ALERT1: init player using songPos: " + songPosition)
                songList[songPosition]?.let { initializePlayer(it) }
//                    setUpUI(songList[songPosition])
            } else {
                if (songPosition < songList.size - 1) {
                    resetPlayer()
                    Log.d("TAG", "ALERT2: init player using songPos: " + (songPosition + 1))
                    songList[++songPosition]?.let { initializePlayer(it) }
//                        setUpUI(songList[songPosition])
                } else {
                    resetPlayer()
                    Log.d("TAG", "ALERT3: init player using songPos: " + 0)
                    songList[0]?.let { initializePlayer(it) }
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
                Log.d("TAG", "ALERT4: init player using songPos: " + songPosition)
                songList[songPosition]?.let { initializePlayer(it) }
//                    setUpUI(songList[songPosition])
            } else {
                if (songPosition < songList.size - 1) {
                    resetPlayer()
                    Log.d("TAG", "ALERT5: init player using songPos: " + (songPosition + 1))
                    songList[++songPosition]?.let { initializePlayer(it) }
//                        setUpUI(songList[songPosition])
                } else {
                    resetPlayer()
                }
            }
        }
        notifySongChanged();
    }

    fun playPrev(){
//        initializePlayer(songUri)

        if (player.currentPosition / 1000 <= 2) {
            if (songPosition == 0) {
                resetPlayer()
                playMusic()
            } else {
                resetPlayer()
                songList[--songPosition]?.let { initializePlayer(it) }
//                    setUpUI(songList[songPosition])
            }
        } else {
            resetPlayer()
            playMusic()
        }
        notifySongChanged()
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
*/