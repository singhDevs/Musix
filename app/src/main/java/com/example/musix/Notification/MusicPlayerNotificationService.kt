package com.example.musix.Notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.musix.R
import com.example.musix.activities.MusicPlayer
import com.example.musix.activities.NewMusicPlayer
import com.example.musix.models.Song


open class MusicPlayerNotificationService(
    private val context: Context,
    private val song: Song,
    private val img: Int
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notification: Notification? = null
    var mediaSessionCompat = MediaSessionCompat(context, "tag")
    fun generateNotification() {
        val activityIntent = Intent(context, NewMusicPlayer::class.java)
        activityIntent.putExtra("fromNotification", "fromNotification")
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val prevIntent = PendingIntent.getBroadcast(
            context,
            4,
            Intent(context, PrevMusicNotificationReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val playIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, PlayMusicNotificationReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val nextIntent = PendingIntent.getBroadcast(
            context,
            3,
            Intent(context, NextMusicNotificationReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val icon = BitmapFactory.decodeResource(context.resources, R.drawable.bg_notif)

        Log.d("notif", "song.title: " + song.title)
        Log.d("notif", "song.artist: " + song.artist)
        notification = NotificationCompat.Builder(context, MUSIC_PLAYER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(icon)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSound(null)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.notification_color))
            .setContentIntent(activityPendingIntent)
            .addAction(
                R.drawable.ic_previous,
                "Prev",
                prevIntent
            )
            .addAction(
                img,
                "Play",
                playIntent
            )
            .addAction(
                R.drawable.ic_next,
                "Next",
                nextIntent
            )
            .setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSessionCompat.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    fun getNotification(): Notification {
        generateNotification()
        notificationManager.notify(NOTIFICATION_ID, notification);
        return notification!!
    }

    companion object {
        const val MUSIC_PLAYER_CHANNEL_ID = "music_player_channel"
        const val NOTIFICATION_ID = 1
    }
}