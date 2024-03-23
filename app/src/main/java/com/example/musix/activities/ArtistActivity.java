package com.example.musix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.adapters.ArtistActivityAdapter;
import com.example.musix.callbacks.RetrieveArtist;
import com.example.musix.dialogs.ViewDialog;
import com.example.musix.handlers.FirebaseHandler;
import com.example.musix.models.Artist;
import com.example.musix.models.Song;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArtistActivity extends AppCompatActivity implements RetrieveArtist {
    private RecyclerView recyclerView;
    private ArtistActivityAdapter adapter;
    private FloatingActionButton playBtn;
    private String artist_name = "";
    private TextView see_more;
    private Artist artist;
    private ImageView banner, artist_image;
    private CardView aboutCard;
    private ViewDialog viewDialog;
    private ShimmerFrameLayout shimmerFrameLayout1, shimmerFrameLayout2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        Intent intent1 = getIntent();
        artist_name = intent1.getStringExtra("artist_name");

        see_more = findViewById(R.id.see_more);
        shimmerFrameLayout1 = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container1);
        shimmerFrameLayout2 = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container2);
        shimmerFrameLayout1.startShimmer();
        shimmerFrameLayout2.startShimmer();
        Log.d("TAG", "Starting Shimmering...");

        banner = findViewById(R.id.banner);
        artist_image = findViewById(R.id.artist_image);

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> {
            finish();
        });

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setTitle(artist_name);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(artist_name);
        fetchArtist(artist_name);
    }

    private void fetchArtist(String artist_name) {
        FirebaseHandler.getArtist(artist_name, this);
    }
    private void fetchSongs(Map<String, Boolean> songs) {
        FirebaseHandler.getArtistSongs(songs, this);
    }

    @Override
    public void onArtistRetrieved(Artist artist) {
        this.artist = artist;
        Log.d("TAG", "songs size: " + artist.getSongs().size());
        fetchSongs(artist.getSongs());
    }

    @Override
    public void onArtistRetrievalFailed(String msg) {
        Log.d("TAG", "Artist retrieval failed, message: " + msg);
    }

    @Override
    public void onSongsFetched(List<Song> songs) {
        Log.d("TAG", "song data size: " + songs.size());
        List<Song> songsList = new ArrayList<Song>();
        if(songs.size() > 5){
            int i = 0;
            while(i < songs.size() && i != 5){
                songsList.add(songs.get(i));
                i++;
            }
        }
        else{
            see_more.setVisibility(View.GONE);
            songsList = songs;
        }
        setupUI(songsList);
    }

    @Override
    public void onSongsFetchedError(String msg) {
        Log.d("TAG", "Artist Song retrieval failed, message: " + msg);
    }

    private void setupUI(List<Song> songs) {
        Glide.with(this)
                .load(artist.getBanner())
                .into(banner);
        Glide.with(this)
                .load(artist.getBanner())
                .into(artist_image);

        Log.d("TAG", "Stopping Shimmering...");
        shimmerFrameLayout2.stopShimmer();
        shimmerFrameLayout1.stopShimmer();

        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        ArtistActivityAdapter.OnArtistSongClickListener onArtistSongClickListener = (songList, position) -> {
            Intent intent = new Intent(this, NewMusicPlayer.class);
            intent.putExtra("source", "internet");
            intent.putExtra("song", (Parcelable) songList.get(position));
            intent.putExtra("songList", (Serializable) songList);
            intent.putExtra("songUrl", songList.get(position).getId());
            intent.putExtra("playlistName", artist_name);
            intent.putExtra("currentUser", FirebaseAuth.getInstance().getCurrentUser().getUid());
            intent.putExtra("songPosition", position);
            startActivity(intent);
        };

        adapter = new ArtistActivityAdapter(this, songs, onArtistSongClickListener);
        recyclerView.setAdapter(adapter);

        playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, NewMusicPlayer.class);
            intent.putExtra("source", "internet");
            intent.putExtra("song", (Parcelable) songs.get(0));
            intent.putExtra("songList", (Serializable) songs);
            intent.putExtra("songUrl", songs.get(0).getId());
            intent.putExtra("playlistName", artist_name);
            intent.putExtra("currentUser", FirebaseAuth.getInstance().getCurrentUser().getUid());
            intent.putExtra("songPosition", 0);
            startActivity(intent);
        });

        aboutCard = findViewById(R.id.aboutCard);
        aboutCard.setOnClickListener(view -> {
            viewDialog = new ViewDialog(getApplicationContext(), this);
            viewDialog.showArtistDialog(artist);
        });
    }
}