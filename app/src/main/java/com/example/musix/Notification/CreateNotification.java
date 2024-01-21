package com.example.musix.Notification;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.musix.R;
import com.example.musix.models.Song;


public class CreateNotification {
    private Context context;

    public CreateNotification(Context context) {
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
    }

    private NotificationManagerCompat notificationManagerCompat;
    public static final int NOTIFICATION_REQUEST_CODE = 1;
    public static final String CHANNEL_ID = "channel1";
    public static final String ACTIONPREVIOUS = "actionprevious";
    public static final String CHANNELPREVIOUS = "channelprevious";
    public static final String CHANNELNEXT = "channelnext";

    public static Notification notification;
    public void createNotification(Context context, Song song, int playBtn, int pos, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_musix);

            // Create Notification
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_musix)
                    .setContentTitle(song.getTitle())
                    .setContentText(song.getArtist())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            checkPermission(Manifest.permission.POST_NOTIFICATIONS, 1);
        }
    }

    //TODO: Handle permission request
    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            //Permission is Granted
            Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show();
            notificationManagerCompat.notify(1, notification);
        } else {
            //Permission is not granted
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
        }
    }
}
