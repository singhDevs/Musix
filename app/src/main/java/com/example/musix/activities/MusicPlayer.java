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

import com.example.musix.R;
import com.example.musix.models.Song;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.makeramen.roundedimageview.RoundedImageView;

public class MusicPlayer extends AppCompatActivity {
    private SimpleExoPlayer player;
    private Song song;
    private RoundedImageView songBanner;
    TextView songTitle, songArtist, currDuration, songDuration;
    ImageView playBtn, nextBtn, prevBtn, backBtn;
    MediaPlayer mediaPlayer;
    SeekBar seekbar;
    Handler handler;
    int duration;
    public final int PLAYING_MUSIC = 1;
    public final int PAUSED_MUSIC = 2;
    public int musicStatus;

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        song = intent.getParcelableExtra("song");

        String songUrl = intent.getStringExtra("songUrl");
        Log.d("TAG", "songURL: " + songUrl);
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

        playBtn = findViewById(R.id.playBtn);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);
        backBtn = findViewById(R.id.backBtn);
        seekbar = findViewById(R.id.seekBar);
        songBanner = findViewById(R.id.songBanner);

        handler = new Handler();

        Intent intent = getIntent();
        song = intent.getParcelableExtra("song");
        duration = song.getDurationInSeconds();
        songDuration.setText(formatTime(duration));
        seekbar.setMax(duration);

        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG", "Clicked Play Button");
                if(musicStatus == PAUSED_MUSIC) playMusic();
                else if(musicStatus == PLAYING_MUSIC) pauseMusic();
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
                    resetPlayer();
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
}