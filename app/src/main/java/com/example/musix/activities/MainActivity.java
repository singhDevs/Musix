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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musix.Constants;
import com.example.musix.R;
import com.example.musix.application.RunningApp;
import com.example.musix.databinding.ActivityMainBinding;
import com.example.musix.fragments.HomeFragment;
import com.example.musix.fragments.FilesFragment;
import com.example.musix.fragments.SearchFragment;
import com.example.musix.handlers.FirebaseHandler;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.example.musix.services.MusicService;
import com.example.musix.settings.MusicPlayerSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private int currentFragmentId = R.id.fragment_container;
    private boolean isBackFromOtherActivity;
    private ChipNavigationBar navigationBar;
    private MusicPlayerSettings musicPlayerSettings;
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
    public final int NOT_LIKED = 0;
    public final int LIKED = 1;
    public int likeState;
    String uid;
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

        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getMusicService();

        musicPlayerSettings = new MusicPlayerSettings(getApplicationContext());
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
        likeBtn = findViewById(R.id.likeBtn);
        fileBtn = findViewById(R.id.playFilesBtn);
        songTxtTitle.setSelected(true);
        songTxtArtist.setSelected(true);

        Log.d("songBar", "2.songBar visi: " + songBar.getVisibility());
        if (musicService != null && Constants.INSTANCE.getMusicStatus() != MusicService.NOT_STARTED) {
            Log.d("songBar", "2.music Status: " + Constants.INSTANCE.getMusicStatus());
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

        playBtn.setOnClickListener(v -> {
            Log.d("TAG", "playButton clicked!");
            if(musicService!= null) {
                if (Constants.INSTANCE.getMusicStatus() == MusicService.PLAYING_MUSIC) {
                    Constants.INSTANCE.setMusicStatus(MusicService.PAUSED_MUSIC);
                    Log.d("TAG", "Found PLAYING, setting PAUSED state...");
                    int play = MusicService.PAUSED_MUSIC;
                    int shuffle = musicPlayerSettings.getShuffleSetting();
                    int repeat = musicPlayerSettings.getRepeatSetting();
                    musicPlayerSettings.saveSettings(play, shuffle, repeat);
//                    musicService.pauseMusic();
                    playBtn.setImageResource(R.drawable.ic_play_arrow);
                } else {
                    Constants.INSTANCE.setMusicStatus(MusicService.PLAYING_MUSIC);
                    Log.d("TAG", "Found PAUSED, setting PLAY state...");
                    int play = MusicService.PLAYING_MUSIC;
                    int shuffle = musicPlayerSettings.getShuffleSetting();
                    int repeat = musicPlayerSettings.getRepeatSetting();
                    musicPlayerSettings.saveSettings(play, shuffle, repeat);
//                    musicService.playMusic();
                    playBtn.setImageResource(R.drawable.ic_pause);
                }
            }
            else{
                Log.d("TAG", "music service is null");
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
        if (navigationBar != null) {
            setUpTabBar();
        } else {
            Log.e("MainActivity", "Navigation Bar is null!");
        }
    }

    private void getMusicService() {
        runningApp = (RunningApp) getApplication();
        if (runningApp != null) {
            Log.d("TAG", "inside MainActivity, initializing music service in Music Player & Service Connection");
            musicService = runningApp.getMusicService();
//            serviceConnection = runningApp.getServiceConnection();
        } else {
            Log.d("TAG", "inside Main Activity, Running App is NULL");
        }
    }

    private void setUpSongBar() {
        songBar.setVisibility(View.VISIBLE);
        new SetLikeState().execute();
        Log.d("songBar", "1.songBar visi: " + songBar.getVisibility());
        Log.d("songBar", "setting up song Bar...");
        Log.d("songBar", "Song title: " + songTitle);
        songTxtTitle.setText(songTitle);
        songTxtArtist.setText(songArtist);
        Glide.with(MainActivity.this)
                .load(songBanner)
                .into(songImgBanner);

        if (musicService != null && Constants.INSTANCE.getMusicStatus() == MusicService.PLAYING_MUSIC)
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
        });
    }

    private class SetLikeState extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (runningApp.getDatabase().songDao().isSongLiked(songList.get(songPosition).getKey())) {
                likeState = LIKED;
                likeBtn.setImageResource(R.drawable.heart_filled);
            } else {
                likeState = NOT_LIKED;
                likeBtn.setImageResource(R.drawable.heart_outline);
            }
            return null;
        }
    }

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
    protected void onResume() {
        Log.d("TAG", "inside onResume...");
        super.onResume();

        if(musicService == null){
            Log.d("TAG", "music service is null");
            Log.d("TAG", "getting music service...");
            getMusicService();

            if(musicService != null){
                if (Constants.INSTANCE.getMusicStatus() == MusicService.PLAYING_MUSIC) {
                    Constants.INSTANCE.setMusicStatus(MusicService.PAUSED_MUSIC);
                    Log.d("TAG", "Found PLAYING, setting PAUSED state...");
                    int play = MusicService.PAUSED_MUSIC;
                    int shuffle = musicPlayerSettings.getShuffleSetting();
                    int repeat = musicPlayerSettings.getRepeatSetting();
                    musicPlayerSettings.saveSettings(play, shuffle, repeat);
//                    musicService.pauseMusic();
                    playBtn.setImageResource(R.drawable.ic_play_arrow);
                }
                else {
                    Constants.INSTANCE.setMusicStatus(MusicService.PLAYING_MUSIC);
                    Log.d("TAG", "Found PAUSED, setting PLAY state...");
                    int play = MusicService.PLAYING_MUSIC;
                    int shuffle = musicPlayerSettings.getShuffleSetting();
                    int repeat = musicPlayerSettings.getRepeatSetting();
                    musicPlayerSettings.saveSettings(play, shuffle, repeat);
//                    musicService.playMusic();
                    playBtn.setImageResource(R.drawable.ic_pause);
                }
            }
        }
        else{
            Log.d("TAG", "music service is not null");
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG", "onDestroy called.");
        super.onDestroy();
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
    }
}