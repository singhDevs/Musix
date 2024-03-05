package com.example.musix.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.musix.Notification.MusicPlayerNotificationService;
import com.example.musix.R;
import com.example.musix.application.RunningApp;
import com.example.musix.handlers.FirebaseHandler;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.example.musix.services.MusicService;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class MusicPlayer extends AppCompatActivity {
    private MusicService musicService;
    private Intent serviceIntent;
//    private SimpleExoPlayer player;
    private NotificationManager notificationManager;
    private MusicPlayerNotificationService notificationService;
    private int source;
    private AudioManager audioManager;
    private Song song;
    private List<Song> songList;
    private int songPosition;
    private RoundedImageView songBanner;
    TextView songTitle, songArtist, currDuration, songDuration, playlistName, bottomTitle, bottomArtist;
    ImageView playBtn, nextBtn, prevBtn, backBtn, likeBtn, repeatBtn, volumeIcon, shuffleBtn, moreBtn, bottomBanner;
    SeekBar seekbar, volumeSeekbar;
    Handler handler;
    int duration;
    public final int PLAYING_MUSIC = 1;
    public final int PAUSED_MUSIC = 2;
    public final int NOT_LIKED = 0;
    public final int LIKED = 1;
    public final int NOT_REPEATED = 0;
    public final int REPEAT = 1;
    public final int REPEAT_ONE = 2;
    private final int NO_SHUFFLE = 0;
    private final int SHUFFLE = 1;
    private final int INTERNET = 0;
    private final int LOCAL_STORAGE = 1;
    public int likeState;
    public int repeatState;
    public int musicStatus;
    private int shuffleStatus;
    private String uid = "";
    private String playlistTitle;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MusicPlayer.this, "onServiceConnected called.", Toast.LENGTH_SHORT).show();

            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getServiceInstance();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MusicPlayer.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
            musicService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        song = intent.getParcelableExtra("song");

        if(song != null)
            notificationService = new MusicPlayerNotificationService(this, song);

        String songUrl = intent.getStringExtra("songUrl");
        Log.d("TAG", "songURL: " + songUrl);

        setUpUI(song);

        setBanner(song.getBanner(), songBanner);

//        if(songUrl != null && !songUrl.isEmpty()){
//            Log.d("TAG", "set up done, now calling function streamAudio...");
//            streamAudioFromFirebase(songUrl);
//        }
//        else{
//            Log.d("TAG", "Error fetching Song URL in onStart");
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        player.release();
        audioManager.abandonAudioFocus(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMusicService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

//        if(Build.VERSION .SDK_INT >= Build.VERSION_CODES.O){
//            createChannel();
//        }

//        player = new SimpleExoPlayer.Builder(this).build();

        playlistName = findViewById(R.id.playlistName);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        bottomBanner = findViewById(R.id.banner);
        bottomArtist = findViewById(R.id.artist);
        bottomTitle = findViewById(R.id.title);
        currDuration = findViewById(R.id.currDuration);
        songDuration = findViewById(R.id.songDuration);
        musicStatus = PLAYING_MUSIC;
        shuffleStatus = NO_SHUFFLE;
        likeState = NOT_LIKED;
        repeatState = NOT_REPEATED;

        playBtn = findViewById(R.id.playBtn);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);
        backBtn = findViewById(R.id.backBtn);
        moreBtn = findViewById(R.id.moreBtn);
        seekbar = findViewById(R.id.seekBar);
        volumeSeekbar = findViewById(R.id.volumeSeekbar);
        volumeIcon = findViewById(R.id.volumeIcon);
        likeBtn = findViewById(R.id.likeBtn);
        shuffleBtn = findViewById(R.id.shuffleBtn);
        repeatBtn = findViewById(R.id.repeatBtn);
        songBanner = findViewById(R.id.songBanner);

        songTitle.setSelected(true);    //for Marquee
        handler = new Handler();

        Intent intent = getIntent();
        songList = (List<Song>) intent.getSerializableExtra("songList");
        if(songList == null) Log.d("TAG", "At the beginning in OnCreate, songList is null");
        songPosition = intent.getIntExtra("songPosition", 0);
        uid = intent.getStringExtra("currentUser");

        playlistTitle = intent.getStringExtra("playlistName");
        playlistName.setText(playlistTitle);

//        serviceIntent = new Intent(this, MusicService.class);
//        serviceIntent.putExtra("playlistName", playlistName.getText().toString());
//        serviceIntent.putExtra("songList", (Serializable) songList);
//        serviceIntent.putExtra("songPosition", songPosition);

        setUpMusicService();    //setting up Music Service

        backBtn.setOnClickListener(view -> finish());

        playBtn.setOnClickListener(view -> {
            if(musicStatus == PAUSED_MUSIC) {
                playMusic();
            }
            else if(musicStatus == PLAYING_MUSIC) pauseMusic();
        });

//        nextBtn.setOnClickListener(view -> playNext());
//        prevBtn.setOnClickListener(view -> playPrev());

        likeBtn.setOnClickListener(view -> {
            //TODO: load Liked Playlist on Login
            if(likeState == NOT_LIKED){
                likeBtn.setImageResource(R.drawable.heart_filled);
                likeState = LIKED;
                Playlist likedPlaylist = new Playlist("", "Liked Songs", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), 0, new HashMap<>());
                FirebaseHandler.addLikedSong(getApplicationContext(), likedPlaylist, uid, songList.get(songPosition), new RunningApp());
            }
            else{
                likeBtn.setImageResource(R.drawable.heart_outline);
                likeState = NOT_LIKED;
                FirebaseHandler.removeLikedSong(getApplicationContext(), uid, songList.get(songPosition), new RunningApp());
            }
        });

        shuffleBtn.setOnClickListener(view -> {
            if(shuffleStatus == NO_SHUFFLE){
                shuffleStatus = SHUFFLE;
                musicService.setMusicStatus(MusicService.SHUFFLE);
                shuffleBtn.setImageResource(R.drawable.shuffle);
            }
            else{
                shuffleStatus = NO_SHUFFLE;
                musicService.setMusicStatus(MusicService.NO_SHUFFLE);
                shuffleBtn.setImageResource(R.drawable.no_shuffle);
            }
        });

        repeatBtn.setOnClickListener(view -> {
            if(repeatState == NOT_REPEATED){
                repeatBtn.setImageResource(R.drawable.repeat);
                repeatState = REPEAT;
                musicService.setMusicStatus(MusicService.REPEAT);
            }
            else if(repeatState == REPEAT){
                repeatBtn.setImageResource(R.drawable.repeat_one);
                repeatState = REPEAT_ONE;
                musicService.setMusicStatus(MusicService.REPEAT_ONE);
            }
            else{
                repeatBtn.setImageResource(R.drawable.no_repeat);
                repeatState = NOT_REPEATED;
                musicService.setMusicStatus(MusicService.NOT_REPEATED);
            }
        });

        moreBtn.setOnClickListener(view -> {
            final LinearLayout layoutMore = findViewById(R.id.layoutMore);
            final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMore);
            if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                layoutMore.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            else{
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                new Handler(Looper.getMainLooper()).postDelayed(() -> layoutMore.setVisibility(View.GONE), 150);
            }
        });

        LinearLayout addToPlaylist = findViewById(R.id.addToPlaylist);
        addToPlaylist.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, AddToPlaylist.class);
            intent1.putExtra("song", (Parcelable) song);
            startActivity(intent1);
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
//                    player.seekTo(i* 1000L);
                    musicService.playerSeekTo(i*1000L);
                    currDuration.setText(formatTime(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //Managing Volume
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeSeekbar.setMax(maxVolume);
        Log.d("TAG", "Max Volume: " + maxVolume);
        volumeSeekbar.setProgress(currVolume);

        int audioFocus = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        if(audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    int percentage = (int) (((float)progress / maxVolume) * 100);
                    Log.d("TAG", "Volume %: " + percentage);
                    Log.d("TAG", "Volume Progress: " + progress);
                    if(percentage < 40 && percentage > 0){
                        volumeIcon.setImageResource(R.drawable.ic_low_volume);
                    }
                    else if(percentage == 0){
                        volumeIcon.setImageResource(R.drawable.ic_mute);
                    }
                    else{
                        volumeIcon.setImageResource(R.drawable.ic_volume);
                    }
                    audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            progress,
                            AudioManager.FLAG_PLAY_SOUND
                    );
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

//        player.addListener(new SimpleExoPlayer.EventListener() {
//            @Override
//            public void onPlaybackStateChanged(int state) {
//                if (state == SimpleExoPlayer.STATE_ENDED) {
//                    playNext(); //TODO: implement Repeat or not
//                }
//            }
//        });
        updateSeekBar();
    }

    private void setUpMusicService() {
//        if(MusicService.sSer)
        serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        serviceIntent.putExtra("songPosition", songPosition);
        serviceIntent.putExtra("songList", (Serializable) songList);
        serviceIntent.putExtra("playlistName", playlistTitle);
        startService(serviceIntent);
    }

    private void updateSeekBar(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if(player != null){
                int currPosition = (int) (musicService.getCurrentPosition() / 1000);
                seekbar.setProgress(currPosition);
                currDuration.setText(formatTime(currPosition));
//            }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    public void playMusic() {
        Log.d("TAG", "Playing Music");
//        if (player != null) {
            playBtn.setImageResource(R.drawable.ic_pause_filled);
            musicStatus = PLAYING_MUSIC;
            musicService.setMusicStatus(MusicService.PLAYING_MUSIC);
            musicService.playMusic();
//            player.setPlayWhenReady(true); // Start playing
            Log.d("TAG", "after musicService.playMusic()");
//        }
    }

    public void pauseMusic() {
        Log.d("TAG", "Music Paused");
//        if (player != null) {
            playBtn.setImageResource(R.drawable.ic_play_filled);
            musicStatus = PAUSED_MUSIC;
            musicService.setMusicStatus(MusicService.PAUSED_MUSIC);
            musicService.pauseMusic();
//            player.pause();
//            player.setPlayWhenReady(false); // Pause
            Log.d("TAG", "after musicService.pauseMusic()");
//        }
    }

    public void resetPlayer() {
//        if(player != null){
            musicStatus = PAUSED_MUSIC;
            musicService.setMusicStatus(MusicService.PAUSED_MUSIC);
            //TODO: temporary provision
            likeState = NOT_LIKED;
            likeBtn.setImageResource(R.drawable.heart_outline);
//            player.pause();
            musicService.pauseMusic();
            playBtn.setImageResource(R.drawable.ic_play_filled);
            musicService.playerSeekTo(0);
//            player.seekTo(0);
//        }
    }

    public void playNext(){
        if(songList != null){
            if(repeatState == REPEAT){
                if(shuffleStatus == SHUFFLE){
                    resetPlayer();
                    int randomPosition = songPosition;
                    while(songPosition == randomPosition){
                        randomPosition = (int) (Math.random() * (songList.size()) + 0);
                    }
                    songPosition = randomPosition;
//                    streamAudioFromFirebase(songList.get(songPosition).getId());
                    setUpUI(songList.get(songPosition));
                }
                else{
                    if(songPosition < songList.size() - 1){
                        resetPlayer();
//                        streamAudioFromFirebase(songList.get(++songPosition).getId());
                        songPosition++;
                        setUpUI(songList.get(songPosition));
                    }
                    else{
                        resetPlayer();
//                        streamAudioFromFirebase(songList.get(0).getId());
                        setUpUI(songList.get(0));
                        songPosition = 0;
                    }
                }
            }
            else if(repeatState == REPEAT_ONE){
                resetPlayer();
                musicService.playMusic();
//                playMusic();
            }
            else{
                if(shuffleStatus == SHUFFLE){
                    resetPlayer();
                    int randomPosition = songPosition;
                    while(songPosition == randomPosition){
                        randomPosition = (int) (Math.random() * (songList.size()) + 0);
                    }
                    songPosition = randomPosition;
//                    streamAudioFromFirebase(songList.get(songPosition).getId());
                    setUpUI(songList.get(songPosition));
                }
                else{
                    if(songPosition < songList.size() - 1){
                        resetPlayer();
//                        streamAudioFromFirebase(songList.get(++songPosition).getId());
                        songPosition++;
                        setUpUI(songList.get(songPosition));
                    }
                    else{
                        resetPlayer();
                    }
                }
            }
        }
        else{
            Log.d("TAG", "songList is null");
        }
    }

//    public void playPrev(){
//        if(songList != null){
//            if(player.getCurrentPosition() / 1000 <= 2){
//                if(songPosition == 0) {
////                    resetPlayer();
////                    playMusic();
//                }
//                else{
////                    resetPlayer();
////                    streamAudioFromFirebase(songList.get(--songPosition).getId());
//                    songPosition--;
//                    setUpUI(songList.get(songPosition));
//                }
//            }
//            else{
////                resetPlayer();
////                playMusic();
//            }
//        }
//    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        player.release();
    }

    private void streamAudioFromFirebase(String songUri){
//        MediaItem mediaItem = MediaItem.fromUri(songUri);
//        player.setMediaItem(mediaItem);
//        player.prepare();
//        player.play();
        playBtn.setImageResource(R.drawable.ic_pause_filled);

        Log.d("TAG", "Calling show Notification");
//        notificationService.showNotification();
//        CreateNotification notification = new CreateNotification(this);
//        notification.createNotification(this, songList.get(songPosition), R.drawable.ic_pause_filled, songPosition, songList.size() - 1);
    }

    private void setUpUI(Song song) {
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());
        songDuration.setText(formatTime(song.getDurationInSeconds()));
        seekbar.setMax(song.getDurationInSeconds());
        setBanner(song.getBanner(), songBanner);

        bottomTitle.setText(song.getTitle());
        bottomArtist.setText(song.getArtist());
        Glide.with(this)
                .load(song.getBanner())
                .into(bottomBanner);
    }

    private void setBanner(String bannerUrl, RoundedImageView roundedImageView){
        Glide.with(this)
                .load(bannerUrl)
                .centerCrop()
                .into(roundedImageView);
    }
}