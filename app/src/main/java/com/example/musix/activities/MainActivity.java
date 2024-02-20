package com.example.musix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.application.RunningApp;
import com.example.musix.fragments.HomeFragment;
import com.example.musix.fragments.FilesFragment;
import com.example.musix.fragments.SearchFragment;
import com.example.musix.models.Song;
import com.example.musix.services.MusicService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    boolean loadedFragment = false;
    ImageView songImgBanner, likeBtn, playBtn;
    TextView songTxtTitle, songTxtArtist;
    MaterialButton fileBtn;
    private CardView songBar;
    private List<Song> songList;
    private RunningApp runningApp;
    private MusicService musicService;
    private ServiceConnection serviceConnection;
    private String songTitle, songArtist, songBanner, playlistName;
    private int songPosition;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "Player Ready Broadcast received");
            if(intent.getAction() != null && intent.getAction().equals(MusicService.PLAYER_PLAYING)){
                songTitle = intent.getStringExtra("songTitle");
                songArtist = intent.getStringExtra("songArtist");
                songBanner = intent.getStringExtra("songBanner");
                playlistName = intent.getStringExtra("playlistName");
                songPosition = intent.getIntExtra("songPosition", 0);
                songList = (List<Song>) intent.getSerializableExtra("songList");

                songBar.setVisibility(View.VISIBLE);
                songTxtTitle.setText(songTitle);
                songTxtArtist.setText(songArtist);
                Glide.with(MainActivity.this)
                        .load(songBanner)
                        .into(songImgBanner);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runningApp = (RunningApp) getApplication();
        if(runningApp != null){
            Log.d("TAG", "inside MainActivity, initializing music service in Music Player & Service Connection");
            musicService = runningApp.getMusicService();
            serviceConnection = runningApp.getServiceConnection();
        }
        else{
            Log.d("TAG", "inside Main Activity, Running App is NULL");
        }

        IntentFilter filter = new IntentFilter(MusicService.PLAYER_PLAYING);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.POST_NOTIFICATIONS},
                    0
            );
        }

        songBar = findViewById(R.id.songBar);
        songImgBanner = findViewById(R.id.songBanner);
        songTxtTitle = findViewById(R.id.songTitle);
        songTxtArtist = findViewById(R.id.songArtist);
        playBtn = findViewById(R.id.playBtn);
        fileBtn = findViewById(R.id.playFilesBtn);
        songTxtTitle.setSelected(true);
        songTxtArtist.setSelected(true);

        songBar.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewMusicPlayer.class);
            intent.putExtra("songTitle", songList.get(songPosition).getTitle());
            intent.putExtra("songArtist", songList.get(songPosition).getArtist());
            intent.putExtra("songBanner", songList.get(songPosition).getBanner());
            intent.putExtra("songList", (Serializable) songList);
            intent.putExtra("songPosition", songPosition);
            intent.putExtra("playlistName", playlistName);
            startActivity(intent);
        });
        playBtn.setOnClickListener(v -> {
            if(musicService.getMusicStatus() == MusicService.PLAYING_MUSIC){
                musicService.setMusicStatus(MusicService.PAUSED_MUSIC);
                musicService.pauseMusic();
                playBtn.setImageResource(R.drawable.ic_play_arrow);
            }
            else{
                musicService.setMusicStatus(MusicService.PLAYING_MUSIC);
                musicService.playMusic();
                playBtn.setImageResource(R.drawable.ic_pause);
            }
        });

        if(songImgBanner.getVisibility() == View.VISIBLE){
            ConstraintLayout constraintLayout = findViewById(R.id.constraintLyt);
            ConstraintSet constraintSet = new ConstraintSet();

            if(constraintLayout != null){
                constraintSet.clone(constraintLayout);
                constraintSet.connect(fileBtn.getId(), ConstraintSet.TOP, R.id.guideline11, ConstraintSet.BOTTOM);
                constraintSet.applyTo(constraintLayout);
            }
            else{
                Log.d("MainActivity", "ConstraintLayout is null");
            }


            if(musicService != null && musicService.getMusicStatus() == MusicService.PLAYING_MUSIC){
                playBtn.setImageResource(R.drawable.ic_pause);
            }
            else{
                playBtn.setImageResource(R.drawable.ic_play_arrow);
            }
        }
        else{

        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                if(itemId == R.id.navHome){
                    if(!loadedFragment){
                        fragmentTransaction.add(R.id.container, new HomeFragment());
                        loadedFragment = true;
                    }
                    else {
                        fragmentTransaction.replace(R.id.container, new HomeFragment());
                    }
                }
                if(itemId == R.id.navSearch) {
                    if(!loadedFragment){
                        fragmentTransaction.add(R.id.container, new SearchFragment());
                        loadedFragment = true;
                    }
                    else{
                        fragmentTransaction.replace(R.id.container, new SearchFragment());
                    }
                }
                if(itemId == R.id.navFiles){
                    if(!loadedFragment){
                        fragmentTransaction.add(R.id.container, new FilesFragment());
                        loadedFragment = true;
                    }
                    else{
                        fragmentTransaction.replace(R.id.container, new FilesFragment());
                    }
                }
                fragmentTransaction.commit();
                return true;
            }
        });

        HomeFragment homeFragment = new HomeFragment();

        fragmentTransaction.replace(R.id.container, homeFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}