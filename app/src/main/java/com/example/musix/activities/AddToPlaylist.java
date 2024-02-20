package com.example.musix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.musix.R;
import com.example.musix.adapters.AddPlaylistAdapter;
import com.example.musix.callbacks.AddToPlaylistCallback;
import com.example.musix.callbacks.PlaylistCallback;
import com.example.musix.handlers.FirebaseHandler;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddToPlaylist extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Playlist> playlists = new ArrayList<>();
    Playlist selectedPlaylist;
    AddPlaylistAdapter adapter;
    Button doneBtn, newPlaylistBtn;
    ImageView backBtn;
    View prevView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist);

        Intent intent = getIntent();
        Song song = intent.getParcelableExtra("song");
        if(song == null) Log.d("TAG", "BRUHH, song is null here too!!");
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> {
            finish();
        });

        newPlaylistBtn = findViewById(R.id.newPlaylistBtn);
        newPlaylistBtn.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, NewPlaylist.class);
            intent1.putExtra("song", (Parcelable) song);
            if(song == null) Log.d("TAG", "HERE ALSO SONG IS NULL!!");
            startActivity(intent1);
        });



        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        AddPlaylistAdapter.OnPlaylistClicked onPlaylistClicked = (playlist, view) -> {
            if(selectedPlaylist == null){
                selectedPlaylist = playlist;
                view.setBackgroundColor(getColor(R.color.selection));
                prevView = view;
            }
            else{
                selectedPlaylist = playlist;
//                TypedValue typedValue = new TypedValue();
//                getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
//                int primaryColor = typedValue.data;
                prevView.setBackgroundColor(getColor(R.color.bg_primary));
                view.setBackground(getDrawable(R.color.selection));
                prevView = view;
            }
            Log.d("TAG", "selected playlist: " + selectedPlaylist);
        };

        doneBtn = findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(view -> {
            FirebaseHandler.addSongToPlaylist(this, FirebaseDatabase.getInstance().getReference().child("playlist").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(selectedPlaylist.getTitle()), selectedPlaylist.getTitle(), song, new AddToPlaylistCallback() {
                @Override
                public void OnAddedToPlaylist() {}

                @Override
                public void OnSongAddedToPlaylist() {
                    finish();
                }
            });
        });

        adapter = new AddPlaylistAdapter(playlists, onPlaylistClicked);
        recyclerView.setAdapter(adapter);
        new GetPlaylistTask(playlists -> {
            Log.d("TAG", "in callback, playlist size: " + playlists.size() + "\nPlaylist Data:-\n");
            for(Playlist playlist : playlists){
                if(playlist != null)
                    Log.d("TAG", "" + playlist.getTitle());
            }
        }).execute();
    }

    public class GetPlaylistTask extends AsyncTask<Void, Void, Void> {
        private final PlaylistCallback playlistCallback;

        public GetPlaylistTask(PlaylistCallback playlistCallback) {
            this.playlistCallback = playlistCallback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            fetchPlaylists();
            return null;
        }

        private void fetchPlaylists() {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("playlist")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            List<Playlist> playlistList = new ArrayList<>();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Playlist playlist = dataSnapshot.getValue(Playlist.class);
                        playlistList.add(playlist);
                    }
                    playlists.clear();
                    playlists.addAll(playlistList);
                    adapter.notifyDataSetChanged();
                    playlistCallback.onPlaylistsFetched(playlistList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("TAG", "Error fetching data: " + error
                            .getMessage());
                }
            });
        }
    }

    public class AddSongToPlaylist extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            addToPlaylist();
            return null;
        }

        private void addToPlaylist() {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("playlist")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(selectedPlaylist.getTitle());

//            databaseReference
        }
    }
}