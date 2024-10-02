package com.example.musix.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;

import com.bumptech.glide.Glide;
import com.example.musix.MediaPlayback;
import com.example.musix.callbacks.MediaControllerCallback;
//import com.example.musix.notification.MusicPlayerNotificationService;
import com.example.musix.R;
import com.example.musix.application.RunningApp;
import com.example.musix.handlers.FirebaseHandler;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.example.musix.services.MusicService;
import com.example.musix.settings.MusicPlayerSettings;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NewMusicPlayer extends AppCompatActivity implements MediaControllerCallback {
//    private MusicPlayerNotificationService musicPlayerNotificationService;
    private AudioManager audioManager;
    private Song song;
    private List<Song> songList = Collections.emptyList();
    private int songPosition = 0;
    private RoundedImageView songBanner;
    TextView songTitle, songArtist, currDuration, songDuration, playlistName, bottomTitle, bottomArtist;
    ImageView playBtn, nextBtn, prevBtn, backBtn, likeBtn, repeatBtn, volumeIcon, shuffleBtn, moreBtn, bottomBanner;
    SeekBar seekbar, volumeSeekbar;
    Handler handler;
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
    private String playlistTitle = "", songURL = "";
    private RunningApp runningApp;
    private MusicPlayerSettings musicPlayerSettings;
    public static MediaController mediaController = null;
    private Boolean isFetchingDone = false;

    /**
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "Song Changed Broadcast received");
            if (intent.getAction() != null && intent.getAction().equals(MusicService.SONG_CHANGED)) {
                songPosition = intent.getIntExtra("songPosition", 0);
                Log.d("TAG", "song Position: " + songPosition);
                setUpUI(songList.get(songPosition));
            }
        }
    };
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaPlayback.INSTANCE.setMediaControllerCallback(this);

        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView(R.layout.activity_new_music_player);

        runningApp = (RunningApp) getApplication();

        findViewById(R.id.progressCardView).setVisibility(View.VISIBLE);

        fetchIntentData();

        if(MediaPlayback.INSTANCE.getMediaController() != null && isFetchingDone){
            onMediaControllerAvailable();
        }

        /*
        if(mediaController != null) setups();
        else{
            if (runningApp != null) {
                Log.d("TAG", "initializing music service in Music Player & Service Connection");
                musicService = runningApp.getMusicService();
                assert musicService != null;
                serviceConnection = runningApp.getServiceConnection();
                controllerFuture.addListener(() ->
                        {
                            try {
                                mediaController = controllerFuture.get();
                                Log.d("TAG", "got the mediaController!");
                                if(mediaController == null) Log.d("TAG", "mediaController is null!");
                                setups();
                            } catch (ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        ContextCompat.getMainExecutor(musicService)
                );
            }
            else Log.d("TAG", "Running App is NULL");
        }
        **/
    }

    private void setups() {
        musicPlayerSettings = new MusicPlayerSettings(getApplicationContext());

        Log.d("TAG", "calling 3 functions...");
        /*
         * IntentFilter filter = new IntentFilter(MusicService.SONG_CHANGED);
         * LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
         * fetchIntentData();
         * setUpMusicService();
        */
        initializeUI();

        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });

        playBtn.setOnClickListener(view -> {
            if (mediaController.isPlaying()) {
                pauseMusic();
            } else{
                playMusic();
            }
        });

        nextBtn.setOnClickListener(view -> {
            updateSongPositionNext();
            MusicService.Companion.mediaItemBuilder(songList.get(songPosition));
            setUpUI(songList.get(songPosition));
            playMusic();
//            musicService.notifySongChanged();
        });
        prevBtn.setOnClickListener(view -> {
            updateSongPositionPrev();
            MusicService.Companion.mediaItemBuilder(songList.get(songPosition));
            playMusic();
        });

        likeBtn.setOnClickListener(view -> {
            if (likeState == NOT_LIKED) {
                likeBtn.setImageResource(R.drawable.heart_filled);
                likeState = LIKED;
                Playlist likedPlaylist = new Playlist("", "Liked Songs", FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), 0, new HashMap<>());

                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseHandler.addLikedSong(getApplicationContext(), likedPlaylist, uid, songList.get(songPosition), runningApp);
            } else {
                likeBtn.setImageResource(R.drawable.heart_outline);
                likeState = NOT_LIKED;
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseHandler.removeLikedSong(getApplicationContext(), uid, songList.get(songPosition), runningApp);
            }
        });

        songArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), ArtistActivity.class);
                intent1.putExtra("artist_name", songList.get(songPosition).getArtist());
                startActivity(intent1);
            }
        });

        shuffleBtn.setOnClickListener(view -> {
            if (shuffleStatus == NO_SHUFFLE) {
                shuffleStatus = SHUFFLE;
                MediaPlayback.INSTANCE.setShuffleStatus(MusicService.SHUFFLE);
                shuffleBtn.setImageResource(R.drawable.shuffle);
            } else {
                shuffleStatus = NO_SHUFFLE;
                MediaPlayback.INSTANCE.setShuffleStatus(MusicService.NO_SHUFFLE);
                shuffleBtn.setImageResource(R.drawable.no_shuffle);
            }
            musicPlayerSettings.saveSettings(MediaPlayback.INSTANCE.getMusicStatus(), shuffleStatus, repeatState);
        });

        repeatBtn.setOnClickListener(view -> {
            if (repeatState == NOT_REPEATED) {
                repeatBtn.setImageResource(R.drawable.repeat);
                repeatState = REPEAT;
                MediaPlayback.INSTANCE.setRepeatStatus(MusicService.REPEAT);
            } else if (repeatState == REPEAT) {
                repeatBtn.setImageResource(R.drawable.repeat_one);
                repeatState = REPEAT_ONE;
                MediaPlayback.INSTANCE.setRepeatStatus(MusicService.REPEAT_ONE);
            } else {
                repeatBtn.setImageResource(R.drawable.no_repeat);
                repeatState = NOT_REPEATED;
                MediaPlayback.INSTANCE.setRepeatStatus(MusicService.NOT_REPEATED);
            }
            musicPlayerSettings.saveSettings(MediaPlayback.INSTANCE.getMusicStatus(), shuffleStatus, repeatState);
        });

        moreBtn.setOnClickListener(view -> {
            final LinearLayout layoutMore = findViewById(R.id.layoutMore);
            final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMore);
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                layoutMore.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                new Handler(Looper.getMainLooper()).postDelayed(() -> layoutMore.setVisibility(View.GONE), 150);
            }
        });


        LinearLayout addToPlaylist = findViewById(R.id.addToPlaylist);
        addToPlaylist.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, AddToPlaylist.class);
            intent1.putExtra("song", (Parcelable) songList.get(songPosition));
            startActivity(intent1);
        });
        LinearLayout aboutArtist = findViewById(R.id.aboutArtist);
        aboutArtist.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, ArtistActivity.class);
            intent1.putExtra("artist_name", songList.get(songPosition).getArtist());
            startActivity(intent1);
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mediaController.seekTo(i * 1000L);
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

        if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
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
                    mediaController.setDeviceVolume(progress, maxVolume);
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
        setUpUI(songList.get(songPosition));
        playMusic();
    }

    private void updateSongPositionPrev() {
        if (mediaController.getCurrentPosition() / 1000 <= 2) {
            if (songPosition == 0) {
                mediaController.seekTo(0);
            } else {
                songPosition--;
            }
        } else {
            mediaController.seekTo(0);
        }
    }

    private void updateSongPositionNext() {
        if (MediaPlayback.INSTANCE.getRepeatStatus() == REPEAT) {
            if (shuffleStatus == SHUFFLE) {
                int randomPosition = songPosition;
                while (songPosition == randomPosition) {
                    randomPosition = (int) (Math.random() * songList.size() + 0);
                }
                songPosition = randomPosition;
            } else {
                if (songPosition < songList.size() - 1) {
                    songPosition++;
                } else {
                    songPosition = 0;
                }
            }
        } else if (MediaPlayback.INSTANCE.getRepeatStatus() == REPEAT_ONE) {
            mediaController.seekTo(0);
        } else {
            if (shuffleStatus == SHUFFLE) {
                int randomPosition = songPosition;
                while (songPosition == randomPosition) {
                    randomPosition = (int) (Math.random() * songList.size() + 0);
                }
                songPosition = randomPosition;
            } else {
                if (songPosition < songList.size() - 1)
                    songPosition++;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
        unbindService(serviceConnection);
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
        */
    }

    /**
    private void setUpMusicService() {
        Log.d("TAG", "setting up Music Service");
        Intent serviceIntent = new Intent(this, MusicService.class);
        musicService.fetchData(songList, songPosition, playlistTitle);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    */

    private void fetchIntentData() {
        Log.d("TAG", "inside fetch Intent Data");
        Intent intent = getIntent();

//        if (intent != null && intent.hasExtra("notificationSongList")) {
//            String msg = intent.getStringExtra("notificationSongList");
//
//        }
//        else{
            songList = (List<Song>) intent.getSerializableExtra("songList");
            songPosition = intent.getIntExtra("songPosition", 0);
            uid = intent.getStringExtra("currentUser");
            playlistTitle = intent.getStringExtra("playlistName");
            songURL = intent.getStringExtra("songUrl");
            Log.d("TAG", "in NEW PLAYER song URL: " + songURL);
            Log.d("TAG", "songPosition: " + songPosition);

            MusicService.Companion.mediaItemBuilder(songList.get(songPosition));
            isFetchingDone = true;
//        }
    }

    private void initializeUI() {
        Log.d("TAG", "inside initialize UI");

//        musicPlayerNotificationService = new MusicPlayerNotificationService(getApplicationContext(), songList.get(songPosition), R.drawable.ic_pause);
        playlistName = findViewById(R.id.playlistName);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        bottomBanner = findViewById(R.id.banner);
        bottomArtist = findViewById(R.id.artist);
        bottomTitle = findViewById(R.id.title);
        currDuration = findViewById(R.id.currDuration);
        songDuration = findViewById(R.id.songDuration);
        musicStatus = PLAYING_MUSIC;
        shuffleStatus = musicPlayerSettings.getShuffleSetting();
        repeatState = musicPlayerSettings.getRepeatSetting();

        //Setting Like State
//        new SetLikeState().execute();

        /*
        if (musicService == null) Log.d("TAG", "music Service is NULL!");
        else Log.d("TAG", "music Service is NOT NULL!");
        musicService.setMusicStatus(musicPlayerSettings.getPlaySetting());
        musicService.setRepeatStatus(musicPlayerSettings.getRepeatSetting());
        musicService.setShuffleStatus(musicPlayerSettings.getShuffleSetting());
        */
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

        //setting up shuffle button
        if (shuffleStatus == NO_SHUFFLE)
            shuffleBtn.setImageResource(R.drawable.no_shuffle);
        else
            shuffleBtn.setImageResource(R.drawable.shuffle);

        //setting up repeat button
        if (repeatState == NOT_REPEATED) {
            repeatBtn.setImageResource(R.drawable.no_repeat);
        } else if (repeatState == REPEAT) {
            repeatBtn.setImageResource(R.drawable.repeat);
        } else {
            repeatBtn.setImageResource(R.drawable.repeat_one);
        }

        //setting up play button
        if(mediaController.isPlaying()) {
            Log.d("TAG", "setting up PAUSE img on play btn...");
            playBtn.setImageResource(R.drawable.ic_pause_filled);
        }
        else {
            Log.d("TAG", "setting up PLAY img on play btn...");
            playBtn.setImageResource(R.drawable.ic_play_filled);
        }

        songTitle.setSelected(true);    //for Marquee
        handler = new Handler(Looper.getMainLooper());
    }

    private void updateSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currPosition = (int) (mediaController.getCurrentPosition() / 1000);
                seekbar.setProgress(currPosition);
                currDuration.setText(formatTime(currPosition));
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    public void onMediaControllerAvailable() {
        Log.d("TAG", "Media Controller Available, calling setups()");
        mediaController = MediaPlayback.INSTANCE.getMediaController();
        MediaPlayback.INSTANCE.getMediaController().addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == Player.STATE_ENDED){
                    updateSongPositionNext();
                    MusicService.Companion.mediaItemBuilder(songList.get(songPosition));
                    setUpUI(songList.get(songPosition));
                    playMusic();
                }
            }
        });
        findViewById(R.id.progressCardView).setVisibility(View.GONE);
        findViewById(R.id.constLayt).setVisibility(View.VISIBLE);
        setups();
    }

//    private class SetLikeState extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... voids) {
//            if (runningApp.getDatabase().songDao().isSongLiked(songList.get(songPosition).getKey())) {
//                likeState = LIKED;
//                likeBtn.setImageResource(R.drawable.heart_filled);
//            } else {
//                likeState = NOT_LIKED;
//                likeBtn.setImageResource(R.drawable.heart_outline);
//            }
//            return null;
//        }
//    }

    private void playMusic() {
        Log.d("TAG", "Playing Music");
        playBtn.setImageResource(R.drawable.ic_pause_filled);
        musicStatus = PLAYING_MUSIC;
        MediaPlayback.INSTANCE.setMusicStatus(MusicService.PLAYING_MUSIC);
        mediaController.play();
        Log.d("TAG", "Saving PLAY state...");
        int play = MusicService.PLAYING_MUSIC;
        int shuffle = musicPlayerSettings.getShuffleSetting();
        int repeat = musicPlayerSettings.getRepeatSetting();
        musicPlayerSettings.saveSettings(play, shuffle, repeat);
        /*
        musicService.setMusicStatus(MusicService.PLAYING_MUSIC);
        Log.d("TAG", "mediaController.play() called!");
        musicService.playMusic();
        **/
    }

    private void pauseMusic() {
        Log.d("TAG", "Music Paused");
        playBtn.setImageResource(R.drawable.ic_play_filled);
        musicStatus = PAUSED_MUSIC;
        MediaPlayback.INSTANCE.setMusicStatus(MusicService.PAUSED_MUSIC);
        mediaController.pause();
        Log.d("TAG", "Saving PAUSE state...");
        int play = MusicService.PAUSED_MUSIC;
        int shuffle = musicPlayerSettings.getShuffleSetting();
        int repeat = musicPlayerSettings.getRepeatSetting();
        musicPlayerSettings.saveSettings(play, shuffle, repeat);
        /*
        Log.d("TAG", "mediaController.pause() called!");
        musicService.setMusicStatus(MusicService.PAUSED_MUSIC);
        musicService.pauseMusic();
         **/
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

        //setting up play button
        if(mediaController.isPlaying()) {
            Log.d("TAG", "setting up PAUSE img on play btn...");
            playBtn.setImageResource(R.drawable.ic_pause_filled);
        }
        else {
            Log.d("TAG", "setting up PLAY img on play btn...");
            playBtn.setImageResource(R.drawable.ic_play_filled);
        }

        bottomTitle.setText(song.getTitle());
        bottomArtist.setText(song.getArtist());
        Glide.with(this)
                .load(song.getBanner())
                .into(bottomBanner);

        showAmbient(song);
    }

    private void setBanner(String bannerUrl, RoundedImageView roundedImageView) {
        Log.d("glide", "Glide being called");
        Glide.with(this)
                .load(bannerUrl)
                .centerCrop()
                .into(roundedImageView);
    }

    private void showAmbient(Song song){
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{
                            Color.parseColor(song.getStartColor()),
                            Color.TRANSPARENT
                    }
            );
            gradientDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            gradientDrawable.setGradientCenter(0.5f, 0.48f);
            gradientDrawable.setGradientRadius(325 * getResources().getDisplayMetrics().density);

            View ambientView = findViewById(R.id.ambientView);
            ambientView.setBackground(gradientDrawable);
            ambientView.animate()
                    .alpha(1f)
                    .setDuration(500);
        }, 1000);
    }
}