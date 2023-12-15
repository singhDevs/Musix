package com.example.musix.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musix.R;

public class MusicPlayer extends AppCompatActivity {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

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
        handler = new Handler();

        mediaPlayer = MediaPlayer.create(this, R.raw.elsueno);
        duration = mediaPlayer.getDuration() / 1000;
        songDuration.setText(formatTime(duration));
        seekbar.setMax(duration);

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

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    mediaPlayer.seekTo(i*1000);
                    currDuration.setText(formatTime(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        playMusic();
        updateSeekBar(duration);
    }
    private void updateSeekBar(int duration){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currPosition = mediaPlayer.getCurrentPosition() / 1000;
                    if (currPosition == duration) resetPlayer();
                    seekbar.setProgress(currPosition);
                    currDuration.setText(formatTime(currPosition));
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    public void playMusic() {
        if (mediaPlayer != null) {
            playBtn.setImageResource(R.drawable.ic_pause_filled);
            musicStatus = PLAYING_MUSIC;
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null) {
            playBtn.setImageResource(R.drawable.ic_play_filled);
            musicStatus = PAUSED_MUSIC;
            mediaPlayer.pause();
        }
    }

    public void resetPlayer() {
        if (mediaPlayer != null) {
            musicStatus = PAUSED_MUSIC;
            mediaPlayer.pause();
            playBtn.setImageResource(R.drawable.ic_play_filled);
            mediaPlayer.seekTo(0);
        }
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        // Correct the order of parameters in String.format
        String formattedTime = String.format("%02d:%02d", minutes, remainingSeconds);

        return formattedTime;
    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}