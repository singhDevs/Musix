package com.example.musix.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.models.Song;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class MusicPlayer extends AppCompatActivity {
    private SimpleExoPlayer player;
    private Song song;
    private List<Song> songList;
    private int songPosition;
    private RoundedImageView songBanner;
    TextView songTitle, songArtist, currDuration, songDuration;
    ImageView playBtn, nextBtn, prevBtn, backBtn, likeBtn, repeatBtn;
    SeekBar seekbar;
    Handler handler;
    int duration;
    public final int PLAYING_MUSIC = 1;
    public final int PAUSED_MUSIC = 2;
    public final int NOT_LIKED = 0;
    public final int LIKED = 1;
    public final int NOT_REPEATED = 0;
    public final int REPEAT = 1;
    public final int REPEAT_ONE = 2;
    public int likeState;
    public int repeatState;
    public int musicStatus;

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        song = intent.getParcelableExtra("song");

        String songUrl = intent.getStringExtra("songUrl");
        Log.d("TAG", "songURL: " + songUrl);

        setUpUI(song);

        setBanner(song.getBanner(), songBanner);

        if(songUrl != null && !songUrl.isEmpty()){
            Log.d("TAG", "set up done, now calling function streamAudio...");
            streamAudioFromFirebase(songUrl);
        }
        else{
            Log.d("TAG", "Error fetching Song URL in onStart");
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        player.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        player = new SimpleExoPlayer.Builder(this).build();

        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        currDuration = findViewById(R.id.currDuration);
        songDuration = findViewById(R.id.songDuration);
        musicStatus = 2;
        likeState = NOT_LIKED; //TODO: should not be hardcoded, store it in DB or something.
        repeatState = NOT_REPEATED;

        playBtn = findViewById(R.id.playBtn);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);
        backBtn = findViewById(R.id.backBtn);
        seekbar = findViewById(R.id.seekBar);
        likeBtn = findViewById(R.id.likeBtn);
        repeatBtn = findViewById(R.id.repeatBtn);
        songBanner = findViewById(R.id.songBanner);

        handler = new Handler();

        Intent intent = getIntent();
        songList = (List<Song>) intent.getSerializableExtra("songList");
        if(songList == null) Log.d("TAG", "before, songList is null");
        songPosition = intent.getIntExtra("songPosition", 0);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicStatus == PAUSED_MUSIC) playMusic();
                else if(musicStatus == PLAYING_MUSIC) pauseMusic();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO: here implement pushing and removing this like song to the DB
                if(likeState == NOT_LIKED){
                    likeBtn.setImageResource(R.drawable.heart_filled);
                    likeState = LIKED;
                }
                else{
                    likeBtn.setImageResource(R.drawable.heart_outline);
                    likeState = NOT_LIKED;
                }
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeatState == NOT_REPEATED){
                    repeatBtn.setImageResource(R.drawable.repeat);
                    repeatState = REPEAT;
                }
                else if(repeatState == REPEAT){
                    repeatBtn.setImageResource(R.drawable.repeat_one);
                    repeatState = REPEAT_ONE;
                }
                else{
                    repeatBtn.setImageResource(R.drawable.no_repeat);
                    repeatState = NOT_REPEATED;
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    player.seekTo(i* 1000L);
                    currDuration.setText(formatTime(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        player.addListener(new SimpleExoPlayer.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == SimpleExoPlayer.STATE_ENDED) {
                    playNext(); //TODO: implement Repeat or not
                }
            }
        });

        updateSeekBar(duration);
    }
    private void updateSeekBar(int duration){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    int currPosition = (int) (player.getCurrentPosition() / 1000);
                    seekbar.setProgress(currPosition);
                    currDuration.setText(formatTime(currPosition));
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    public void playMusic() {
        Log.d("TAG", "Playing Music");
        if (player != null) {
            playBtn.setImageResource(R.drawable.ic_pause_filled);
            musicStatus = PLAYING_MUSIC;
            player.setPlayWhenReady(true); // Start playing
            Log.d("TAG", "after player.setPlayWhenReady(true)");
        }
    }

    public void pauseMusic() {
        Log.d("TAG", "Music Paused");
        if (player != null) {
            playBtn.setImageResource(R.drawable.ic_play_filled);
            musicStatus = PAUSED_MUSIC;
            player.setPlayWhenReady(false); // Pause
            Log.d("TAG", "after player.setPlayWhenReady(false)");
        }
    }

    public void resetPlayer() {
        if(player != null){
            musicStatus = PAUSED_MUSIC;
            player.pause();
            playBtn.setImageResource(R.drawable.ic_play_filled);
            player.seekTo(0);
        }
    }

    public void playNext(){
        if(songList != null){
            if(repeatState == REPEAT){
                if(songPosition < songList.size() - 1){
                    resetPlayer();
                    streamAudioFromFirebase(songList.get(++songPosition).getId());
                    setUpUI(songList.get(songPosition));
                }
                else{
                    resetPlayer();
                    streamAudioFromFirebase(songList.get(0).getId());
                    setUpUI(songList.get(0));
                    songPosition = 0;
                }
            }
            else if(repeatState == REPEAT_ONE){
                resetPlayer();
                playMusic();
            }
            else{
                if(songPosition < songList.size() - 1){
                    resetPlayer();
                    streamAudioFromFirebase(songList.get(++songPosition).getId());
                    setUpUI(songList.get(songPosition));
                }
                else{
                    resetPlayer();
                }
            }
        }
        else{
            Log.d("TAG", "songList is null");
        }
    }

    public void playPrev(){
        if(songList != null){
            if(player.getCurrentPosition() / 1000 <= 2){
                if(songPosition == 0) {
                    resetPlayer();
                    playMusic();
                }
                else{
                    resetPlayer();
                    streamAudioFromFirebase(songList.get(--songPosition).getId());
                    setUpUI(songList.get(songPosition));
                }
            }
            else{
                resetPlayer();
                playMusic();
            }
        }
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    private void streamAudioFromFirebase(String songUri){
        MediaItem mediaItem = MediaItem.fromUri(songUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        playBtn.setImageResource(R.drawable.ic_pause_filled);
    }

    private void setBanner(String bannerUrl, RoundedImageView roundedImageView){
        Glide.with(this)
                .load(bannerUrl)
                .centerCrop()
                .into(roundedImageView);
    }

    private void setUpUI(Song song) {
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());
        songDuration.setText(formatTime(song.getDurationInSeconds()));
        seekbar.setMax(song.getDurationInSeconds());
        setBanner(song.getBanner(), songBanner);
    }
}