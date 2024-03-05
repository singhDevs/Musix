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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.application.RunningApp;
import com.example.musix.databinding.ActivityMainBinding;
import com.example.musix.fragments.HomeFragment;
import com.example.musix.fragments.FilesFragment;
import com.example.musix.fragments.SearchFragment;
import com.example.musix.models.Song;
import com.example.musix.services.MusicService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private int currentFragmentId = R.id.fragment_container; // Replace with your container id
    private boolean isBackFromOtherActivity;
    private ChipNavigationBar navigationBar;
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
    private ActivityMainBinding binding;
    private int songPosition;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "Player Ready Broadcast received");
            if (intent.getAction() != null && intent.getAction().equals(MusicService.PLAYER_PLAYING)) {
                songTitle = intent.getStringExtra("songTitle");
                songArtist = intent.getStringExtra("songArtist");
                songBanner = intent.getStringExtra("songBanner");
                playlistName = intent.getStringExtra("playlistName");
                songPosition = intent.getIntExtra("songPosition", 0);
                songList = (List<Song>) intent.getSerializableExtra("songList");

                setUpSongBar();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        runningApp = (RunningApp) getApplication();
        if (runningApp != null) {
            Log.d("TAG", "inside MainActivity, initializing music service in Music Player & Service Connection");
            musicService = runningApp.getMusicService();
            serviceConnection = runningApp.getServiceConnection();
        } else {
            Log.d("TAG", "inside Main Activity, Running App is NULL");
        }

        IntentFilter filter = new IntentFilter(MusicService.PLAYER_PLAYING);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
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

        Log.d("songBar", "2.songBar visi: " + songBar.getVisibility());
        if (musicService != null && musicService.getMusicStatus() != MusicService.NOT_STARTED) {
            Log.d("songBar", "2.music Status: " + musicService.getMusicStatus());
            setUpSongBar();
        }

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

        FrameLayout bottomTopFrameLayout = findViewById(R.id.fragment_container); // Replace with your FrameLayout's id
        View songBar = findViewById(R.id.songBar); // Replace with your songBar's id

//        if(songBar == null) Log.d("songBar", "SongBar is NULL");
//        else{
//            Log.d("songBar", "SongBar is NOT NULL");
//            songBar.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//                @Override
//                public void onViewAttachedToWindow(View v) {
//                    Log.d("songBar", "entered onViewAttachedToWindow...");
//                    if (songBar.getVisibility() == View.VISIBLE) {
//                        // SongBar is visible, set bottomTopFrameLayout parent to songBar
//                        ((ViewGroup) songBar.getParent()).removeView(bottomTopFrameLayout);
//                        ((ViewGroup) v).addView(bottomTopFrameLayout);
//                    } else {
//                        // SongBar is invisible, set bottomTopFrameLayout parent to activity/fragment layout
//                        ((ViewGroup) bottomTopFrameLayout.getParent()).removeView(bottomTopFrameLayout);
//                        ((ViewGroup) findViewById(android.R.id.content)).addView(bottomTopFrameLayout); // Assuming parent is activity layout
//                    }
//                }
//
//                @Override
//                public void onViewDetachedFromWindow(View v) {
//                    // Detaching, not relevant for this scenario
//                }
//            });
//        }

        playBtn.setOnClickListener(v -> {
            if (musicService.getMusicStatus() == MusicService.PLAYING_MUSIC) {
                musicService.setMusicStatus(MusicService.PAUSED_MUSIC);
                musicService.pauseMusic();
                playBtn.setImageResource(R.drawable.ic_play_arrow);
            } else {
                musicService.setMusicStatus(MusicService.PLAYING_MUSIC);
                musicService.playMusic();
                playBtn.setImageResource(R.drawable.ic_pause);
            }
        });

        if (songImgBanner.getVisibility() == View.VISIBLE) {
            ConstraintLayout constraintLayout = findViewById(R.id.constraintLyt);
            ConstraintSet constraintSet = new ConstraintSet();

            if (constraintLayout != null) {
                constraintSet.clone(constraintLayout);
                constraintSet.connect(fileBtn.getId(), ConstraintSet.TOP, R.id.guideline11, ConstraintSet.BOTTOM);
                constraintSet.applyTo(constraintLayout);
            } else {
                Log.d("MainActivity", "ConstraintLayout is null");
            }

        }

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//        bottomNavigationView = findViewById(R.id.bottomNav);
//
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int itemId = item.getItemId();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//                if(itemId == R.id.nav_home){
//                    if(!loadedFragment){
//                        fragmentTransaction.add(R.id.fragment_container, new HomeFragment());
//                        loadedFragment = true;
//                    }
//                    else {
//                        fragmentTransaction.replace(R.id.fragment_container, new HomeFragment());
//                    }
//                }
//                if(itemId == R.id.nav_search) {
//                    if(!loadedFragment){
//                        fragmentTransaction.add(R.id.fragment_container, new SearchFragment());
//                        loadedFragment = true;
//                    }
//                    else{
//                        fragmentTransaction.replace(R.id.fragment_container, new SearchFragment());
//                    }
//                }
//                if(itemId == R.id.nav_files){
//                    if(!loadedFragment){
//                        fragmentTransaction.add(R.id.fragment_container, new FilesFragment());
//                        loadedFragment = true;
//                    }
//                    else{
//                        fragmentTransaction.replace(R.id.fragment_container, new FilesFragment());
//                    }
//                }
//                fragmentTransaction.commit();
//                return true;
//            }
//        });
//
//        HomeFragment homeFragment = new HomeFragment();
//
//        fragmentTransaction.replace(R.id.fragment_container, homeFragment);
//        fragmentTransaction.commit();

        fragmentManager = getSupportFragmentManager();

        // Check intent for back button flag
        Intent intent = getIntent();
        isBackFromOtherActivity = intent.getBooleanExtra("isBackFromOtherActivity", false);

        // Restore fragment based on backstack or saved state
        if (isBackFromOtherActivity && savedInstanceState != null) {
            currentFragmentId = savedInstanceState.getInt("currentFragmentId", R.id.fragment_container);
            fragmentManager.popBackStackImmediate(); // Pop any pending transactions
        } else if (savedInstanceState != null) {
            // Restore fragment from saved state (normal launch)
            currentFragmentId = savedInstanceState.getInt("currentFragmentId", R.id.fragment_container);
        } else {
            // Add initial fragment (e.g., HomeFragment)
            fragmentManager.beginTransaction()
                    .replace(currentFragmentId, new HomeFragment())
                    .commit();
        }

        navigationBar = findViewById(R.id.bottomNav);
        binding.bottomNav.setItemSelected(R.id.nav_home, true);
        // Bottom navigation listener
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        if (navigationBar != null) {
            setUpTabBar();
//            bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        } else {
            Log.e("MainActivity", "Navigation Bar is null!");
        }
    }

    private void setUpSongBar() {
        songBar.setVisibility(View.VISIBLE);
        Log.d("songBar", "1.songBar visi: " + songBar.getVisibility());
        Log.d("songBar", "setting up song Bar...");
        Log.d("songBar", "Song title: " + songTitle);
        songTxtTitle.setText(songTitle);
        songTxtArtist.setText(songArtist);
        Glide.with(MainActivity.this)
                .load(songBanner)
                .into(songImgBanner);

        if (musicService != null && musicService.getMusicStatus() == MusicService.PLAYING_MUSIC)
            playBtn.setImageResource(R.drawable.ic_pause);
        else
            playBtn.setImageResource(R.drawable.ic_play_arrow);
    }

    private void setUpTabBar() {

        binding.bottomNav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemId) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (itemId == R.id.nav_home) {
                    fragmentTransaction.replace(currentFragmentId, new HomeFragment());
                    fragmentTransaction.commit();
                } else if (itemId == R.id.nav_search) {
                    fragmentTransaction.replace(currentFragmentId, new SearchFragment());
                    fragmentTransaction.commit();
                } else if (itemId == R.id.nav_files) {
                    fragmentTransaction.replace(currentFragmentId, new FilesFragment());
                    fragmentTransaction.commit();
                }
            }

//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int itemId = item.getItemId();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                if (itemId == R.id.nav_home) {
//                    fragmentTransaction.replace(currentFragmentId, new HomeFragment());
//                    fragmentTransaction.commit();
////                        binding.textMain.setText("Near");
//                    return true;
//                } else if (itemId == R.id.nav_search) {
//                    fragmentTransaction.replace(currentFragmentId, new SearchFragment());
//                    fragmentTransaction.commit();
////                        binding.textMain.setText("Chat");
//                    return true;
//                } else if (itemId == R.id.nav_files) {
//                    fragmentTransaction.replace(currentFragmentId, new FilesFragment());
//                    fragmentTransaction.commit();
////                        binding.textMain.setText("Profile");
//                    return true;
//                } else if (itemId == R.id.nav_premium) {
//                    fragmentTransaction.replace(currentFragmentId, new SearchFragment());
////                        binding.textMain.setText("Settings");
//                    fragmentTransaction.commit();
//                    return true;
//                }
//                return false;
//            }
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    if (itemId == R.id.nav_home) {
                        fragmentTransaction.replace(currentFragmentId, new HomeFragment());
                    } else if (itemId == R.id.nav_search) {
                        fragmentTransaction.replace(currentFragmentId, new SearchFragment());
                    } else if (itemId == R.id.nav_files) {
                        fragmentTransaction.replace(currentFragmentId, new FilesFragment());
                    }
                    // Add more navigation logic for other fragments

                    fragmentTransaction.commit();
                    return true;
                }
            };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFragmentId", currentFragmentId);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}