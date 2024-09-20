//package com.example.musix.notification
//
//import android.app.Notification
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.media.MediaMetadata
//import android.os.Bundle
//import android.support.v4.media.MediaMetadataCompat
//import android.util.Log
//import androidx.annotation.OptIn
//import androidx.core.app.NotificationCompat
//import androidx.core.content.ContextCompat
//import androidx.media.app.NotificationCompat.MediaStyle
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.session.MediaNotification
//import androidx.media3.session.MediaSession
//import androidx.media3.session.SessionCommand
//import com.example.musix.R
//import com.example.musix.activities.NewMusicPlayer
//import com.example.musix.application.RunningApp
//import com.example.musix.models.Song
//import com.example.musix.services.MusicService
//
//
//open class MusicPlayerNotificationService(
//    private val context: Context,
//    private val song: Song,
//    private val img: Int
//) {
////    private inner class CustomMediaSessionCallback: MediaSession.Callback {
////        @OptIn(UnstableApi::class) override fun onConnect(
////            session: MediaSession,
////            controller: MediaSession.ControllerInfo
////        ): MediaSession.ConnectionResult {
////            val SAVE_TO_FAVORITES
////            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
////                .add(SessionCommand(SAVE_TO_FAVORITES, Bundle.EMPTY))
////                .build()
////            return MediaSession.ConnectionResult.AcceptedResultBuilder(
////                session
////            )
////                .setAvailableSessionCommands(sessionCommands)
////                .build()
////        }
////
////    }
//    private var musicService: MusicService? = null
//    init {
//        val runningApp = context.applicationContext as RunningApp
//        if (runningApp != null) {
//            Log.d("TAG", "initializing music service in Music Player & Service Connection")
//            musicService = runningApp.getMusicService()
//            checkNotNull(musicService)
//        }
//    }
//    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    private var notification: Notification? = null
////    private val mediaStyle = MediaStyle()
////        .setShowActionsInCompactView(0, 1, 2)
////        .setMediaSession(musicService?.mediaSession?.token)
//
//    private fun generateNotification() {
//        val activityIntent = Intent(context, NewMusicPlayer::class.java)
//        activityIntent.putExtra("fromNotification", "fromNotification")
//
//        val activityPendingIntent = PendingIntent.getActivity(
//            context,
//            1,
//            activityIntent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val prevIntent = PendingIntent.getBroadcast(
//            context,
//            4,
//            Intent(context, PrevMusicNotificationReceiver::class.java),
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val playIntent = PendingIntent.getBroadcast(
//            context,
//            2,
//            Intent(context, PlayMusicNotificationReceiver::class.java),
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val nextIntent = PendingIntent.getBroadcast(
//            context,
//            3,
//            Intent(context, NextMusicNotificationReceiver::class.java),
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val icon = BitmapFactory.decodeResource(context.resources, R.drawable.bg_notif)
//
//        Log.d("notif", "song.title: " + song.title)
//        Log.d("notif", "song.artist: " + song.artist)
//
//        // setting up notification
////        mediaSession.setMetadata(
////            MediaMetadataCompat.Builder()
////                .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
////                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
////                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, song.banner)
////                .putLong(MediaMetadata.METADATA_KEY_DURATION, (song.durationInSeconds*1000).toLong())
////                .build()
////        )
//
////        mediaSession.setPlaybackState(
////            PlaybackStateCompat.Builder()
////                .setState()
////                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
////                .build()
////        )
//
//
//
//        notification = NotificationCompat.Builder(context, MUSIC_PLAYER_CHANNEL_ID)
////            .setStyle(mediaStyle)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setAutoCancel(false)
//            .setOngoing(true)
//            .setCategory(Notification.CATEGORY_SERVICE)
//            .setBadgeIconType(R.drawable.musix_notif)
//            .setSmallIcon(R.drawable.ic_music_note)
//            .setLargeIcon(icon)
//            .setContentTitle(song.title)
//            .setContentText(song.artist)
//            .setSound(null)
//            .setColorized(true)
//            .setColor(ContextCompat.getColor(context, R.color.notification_color))
//            .setContentIntent(activityPendingIntent)
//            .addAction(
//                R.drawable.ic_previous,
//                "Prev",
//                prevIntent
//            )
//            .addAction(
//                img,
//                "Play",
//                playIntent
//            )
//            .addAction(
//                R.drawable.ic_next,
//                "Next",
//                nextIntent
//            )
//            .build()
//    }
//
//    fun getNotification(): MediaNotification {
//        generateNotification()
//        notificationManager.notify(NOTIFICATION_ID, notification)
//        return MediaNotification(NOTIFICATION_ID, notification!!)
//    }
//
//    companion object {
//        const val MUSIC_PLAYER_CHANNEL_ID = "music_player_channel"
//        const val NOTIFICATION_ID = 1
//    }
//}