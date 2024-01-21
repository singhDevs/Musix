package com.example.musix.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class NewMusicPlayer extends AppCompatActivity {
    private NotificationManager notificationManager;
    private MusicPlayerNotificationService notificationService;
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
    public int likeState;
    public int repeatState;
    public int musicStatus;
    private int shuffleStatus;
    private String uid = "";
    private String playlistTitle, songURL;
    private RunningApp runningApp;
    private MusicService musicService;
    private ServiceConnection serviceConnection;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "Song Changed Broadcast received");
            if(intent.getAction() != null && intent.getAction().equals(MusicService.SONG_CHANGED)){
                songPosition = intent.getIntExtra("songPosition", 0);
                Log.d("TAG", "song Position: " + songPosition);
                setUpUI(songList.get(songPosition));
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        setUpUI(songList.get(songPosition));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_music_player);

        runningApp = (RunningApp) getApplication();
        if(runningApp != null){
            Log.d("TAG", "initializing music service in Music Player & Service Connection");
            musicService = runningApp.getMusicService();
            serviceConnection = runningApp.getServiceConnection();
        }
        else{
            Log.d("TAG", "Running App is NULL");
        }

        IntentFilter filter = new IntentFilter(MusicService.SONG_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        fetchIntentData();
        initializeUI();
        setUpMusicService();

        //TODO: Do i need to comm back to the activity for the musicStatus?
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
                Playlist likedPlaylist = new Playlist(0, "Liked Songs", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), 0, new HashMap<>());
                FirebaseHandler.addLikedSong(getApplicationContext(), likedPlaylist, uid, songList.get(songPosition));
            }
            else{
                likeBtn.setImageResource(R.drawable.heart_outline);
                likeState = NOT_LIKED;
                FirebaseHandler.removeLikedSong(getApplicationContext(), uid, songList.get(songPosition));
            }
        });

        shuffleBtn.setOnClickListener(view -> {
            if(shuffleStatus == NO_SHUFFLE){
                shuffleStatus = SHUFFLE;
                musicService.setShuffleStatus(MusicService.SHUFFLE);
                shuffleBtn.setImageResource(R.drawable.shuffle);
            }
            else{
                shuffleStatus = NO_SHUFFLE;
                musicService.setShuffleStatus(MusicService.NO_SHUFFLE);
                shuffleBtn.setImageResource(R.drawable.no_shuffle);
            }
        });

        repeatBtn.setOnClickListener(view -> {
            if(repeatState == NOT_REPEATED){
                repeatBtn.setImageResource(R.drawable.repeat);
                repeatState = REPEAT;
                musicService.setRepeatStatus(MusicService.REPEAT);
            }
            else if(repeatState == REPEAT){
                repeatBtn.setImageResource(R.drawable.repeat_one);
                repeatState = REPEAT_ONE;
                musicService.setRepeatStatus(MusicService.REPEAT_ONE);
            }
            else{
                repeatBtn.setImageResource(R.drawable.no_repeat);
                repeatState = NOT_REPEATED;
                musicService.setRepeatStatus(MusicService.NOT_REPEATED);
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

        //managing Volume
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

        if(audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    int percentage = (int) (((float) progress / maxVolume) * 100);
                    Log.d("TAG", "Volume %: " + percentage);
                    Log.d("TAG", "Volume Progress: " + progress);
                    if (percentage < 40 && percentage > 0) {
                        volumeIcon.setImageResource(R.drawable.ic_low_volume);
                    } else if (percentage == 0) {
                        volumeIcon.setImageResource(R.drawable.ic_mute);
                    } else {
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
        updateSeekBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void setUpMusicService(){
        Log.d("TAG", "setting up Music Service");
        Intent serviceIntent = new Intent(this, MusicService.class);
        musicService.fetchData(songList, songPosition, playlistTitle);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void fetchIntentData(){
        Intent intent = getIntent();
        songList = (List<Song>) intent.getSerializableExtra("songList");
        songPosition = intent.getIntExtra("songPosition", 0);
        uid = intent.getStringExtra("currentUser");
        playlistTitle = intent.getStringExtra("playlistName");
        songURL = intent.getStringExtra("songUrl");
        Log.d("TAG", "in NEW PLAYER song URL: " + songURL);
    }

    private void initializeUI(){
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
        musicService.setMusicStatus(MusicService.PLAYING_MUSIC);
        musicService.setRepeatStatus(MusicService.NO_SHUFFLE);
        musicService.setShuffleStatus(MusicService.NOT_REPEATED);
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
        playlistName.setText(playlistTitle);
        songTitle.setSelected(true);    //for Marquee
        handler = new Handler(Looper.getMainLooper());
    }

    private void updateSeekBar(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currPosition = (int) (musicService.getCurrentPosition() / 1000);
                seekbar.setProgress(currPosition);
                currDuration.setText(formatTime(currPosition));
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void playMusic(){
        Log.d("TAG", "Playing Music");
        playBtn.setImageResource(R.drawable.ic_pause_filled);
        musicStatus = PLAYING_MUSIC;
        musicService.setMusicStatus(MusicService.PLAYING_MUSIC);
        musicService.playMusic();
    }

    private void pauseMusic(){
        Log.d("TAG", "Music Paused");
        playBtn.setImageResource(R.drawable.ic_play_filled);
        musicStatus = PAUSED_MUSIC;
        musicService.setMusicStatus(MusicService.PAUSED_MUSIC);
        musicService.pauseMusic();
    }

    public void resetPlayer() {
        musicStatus = PAUSED_MUSIC;
        musicService.setMusicStatus(MusicService.PAUSED_MUSIC);
        //TODO: temporary provision
        likeState = NOT_LIKED;
        likeBtn.setImageResource(R.drawable.heart_outline);
        musicService.pauseMusic();
        playBtn.setImageResource(R.drawable.ic_play_filled);
        musicService.playerSeekTo(0);
    }

    @SuppressLint("DefaultLocale")
    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void setUpUI(Song song) {
        Log.d("TAG", "inside setup UI");
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());
        songDuration.setText(formatTime(song.getDurationInSeconds()));
        seekbar.setMax(song.getDurationInSeconds());
        setBanner(song.getBanner(), songBanner);
        playBtn.setImageResource(R.drawable.ic_pause_filled);

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