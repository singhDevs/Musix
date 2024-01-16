package com.example.musix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.adapters.PlaylistActivityAdapter;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Song> songs;
    private List<String> songIDs;
    private PlaylistActivityAdapter adapter;
    private FloatingActionButton playBtn;
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Intent intent1 = getIntent();
        Playlist playlist = intent1.getParcelableExtra("playlist");

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> {
            finish();
        });

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setTitle(playlist.getTitle());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(playlist.getTitle());

        TextView numSongs = findViewById(R.id.numSongs);

        String numTxt = "";
        if(playlist.getSongs() != null){
            numTxt = String.valueOf(playlist.getSongs().size());
            numTxt += (playlist.getSongs().size() > 1) ? " songs" : " song";
        }
        else numTxt = "No songs in this Playlist";
        numSongs.setText(numTxt);

        ImageView banner = findViewById(R.id.banner);
        if(playlist != null){
            if(playlist.getTitle().equals("Liked Songs")){
                Glide.with(this)
                        .load(getDrawable(R.drawable.bg_liked_playlist))
                        .into(banner);
            }
            else{
                Glide.with(this)
                        .load(getDrawable(R.drawable.bg_playlist))
                        .into(banner);
            }
        }

        songs = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        PlaylistActivityAdapter.OnPlaylistClickListener onPlaylistClickListener = (songList, position) -> {
            Intent intent = new Intent(this, MusicPlayer.class);
            intent.putExtra("source", "internet");
            intent.putExtra("song", (Parcelable) songList.get(position));
            intent.putExtra("songList", (Serializable) songList);
            intent.putExtra("songUrl", songList.get(position).getId());
            intent.putExtra("playlistName", playlist.getTitle());
            intent.putExtra("currentUser", FirebaseAuth.getInstance().getCurrentUser().getUid());
            intent.putExtra("songPosition", position);
            startActivity(intent);
        };

        if(playlist != null){
            songIDs = new ArrayList<>(playlist.getSongs().keySet());
            new GetSongs().execute();
        }

        adapter = new PlaylistActivityAdapter(this, songs, onPlaylistClickListener);
        recyclerView.setAdapter(adapter);

        playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, MusicPlayer.class);
            intent.putExtra("source", "internet");
            intent.putExtra("song", (Parcelable) songs.get(0));
            intent.putExtra("songList", (Serializable) songs);
            intent.putExtra("songUrl", songs.get(0).getId());
            intent.putExtra("playlistName", playlist.getTitle());
            intent.putExtra("currentUser", FirebaseAuth.getInstance().getCurrentUser().getUid());
            intent.putExtra("songPosition", 0);
            startActivity(intent);
        });
    }

    public class GetSongs extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            List<Song> listOfSongs = new ArrayList<>();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("songs");
            Collections.sort(songIDs);

            Log.d("TAG", "size of songID: " + songIDs.size());

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                int i = 0;
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if(i < songIDs.size() && songIDs.get(i).equals(dataSnapshot.getKey())){
                                Song song = dataSnapshot.getValue(Song.class);
                                listOfSongs.add(song);
                                i++;
                            }
                        }
                        songs.clear();
                        songs.addAll(listOfSongs);
                        adapter.notifyDataSetChanged();

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            return null;
        }
    }
}