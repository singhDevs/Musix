package com.example.musix.Notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.musix.R
import com.example.musix.activities.MusicPlayer
import com.example.musix.models.Song

open class MusicPlayerNotificationService(private val context: Context, private val song: Song) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notification: Notification? = null
    fun generateNotification(){
        val activityIntent = Intent(context, MusicPlayer::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)PendingIntent.FLAG_IMMUTABLE else 0
        )

        val playIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, PlayMusicNotificationReceiver::class.java),
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val nextIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, NextMusicNotificationReceiver::class.java),
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val icon = BitmapFactory.decodeResource(context.resources, R.drawable.musix_notif)

         notification = NotificationCompat.Builder(context, MUSIC_PLAYER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(icon)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.ic_play_arrow,
                "Play",
                playIntent)
            .addAction(R.drawable.ic_next,
                "Next",
                nextIntent)
            .build()

//        Log.d("TAG", "Notifying now...")
//        notificationManager.notify(1, notification)
    }

    fun getNotification(): Notification{
        generateNotification()
        return notification!!
    }

    companion object{
        const val MUSIC_PLAYER_CHANNEL_ID = "music_player_channel"
    }
}