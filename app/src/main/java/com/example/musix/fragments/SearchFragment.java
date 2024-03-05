package com.example.musix.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.musix.R;
import com.example.musix.activities.NewMusicPlayer;
import com.example.musix.adapters.PlaylistActivityAdapter;
import com.example.musix.adapters.SearchSongAdapter;
import com.example.musix.models.Song;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchFragment extends Fragment {
    RecyclerView searchRecycler;
    SearchSongAdapter adapter;
    EditText searchBox;
    ImageView clearSearch;
    List<Song> songs = new ArrayList<>();
    List<Song> allSongs = new ArrayList<>();
    Timer timer;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchRecycler = view.findViewById(R.id.searchRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        searchRecycler.setLayoutManager(layoutManager);

        searchBox = view.findViewById(R.id.searchBox);
        clearSearch = view.findViewById(R.id.clearSearch);
        clearSearch.setOnClickListener(v -> {
            searchBox.setText("");
            songs.clear();
            adapter.notifyDataSetChanged();
//            searchRecycler.setVisibility(View.GONE);
            Log.d("TAG", "clearing songs...");
            Log.d("TAG", "songs size: " + songs.size());
            Log.d("TAG", "1.Search Recycler Visibility: " + searchRecycler.getVisibility());
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
//                    searchRecycler.setVisibility(View.VISIBLE);
                    Log.d("TAG", "2.Search Recycler Visibility: " + searchRecycler.getVisibility());
                }
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    clearSearch.setVisibility(View.VISIBLE);
                    searchBox.setVisibility(View.VISIBLE);
                    Log.d("TAG", "3.Search Recycler Visibility: " + searchRecycler.getVisibility());
                }
                if (searchBox.getText().toString().isEmpty())
                    clearSearch.setVisibility(View.GONE);

                if (!allSongs.isEmpty()) {
                    searchBox.setVisibility(View.VISIBLE);
                    Log.d("TAG", "4.Search Recycler Visibility: " + searchRecycler.getVisibility());
                    searchSongs(s.toString());
                } else {
                    Toast.makeText(getContext(), "No song matches your search", Toast.LENGTH_SHORT).show();
                }
            }
        });


        SearchSongAdapter.OnSearchItemClickListener onSearchItemClickListener = (songList, position) -> {
            List<Song> tempPlaylist = new ArrayList<>();
            tempPlaylist.add(songs.get(position));
            Intent intent = new Intent(getContext(), NewMusicPlayer.class);
            intent.putExtra("source", "internet");
            intent.putExtra("song", (Parcelable) tempPlaylist.get(0));
            intent.putExtra("songList", (Serializable) tempPlaylist);
            intent.putExtra("songUrl", tempPlaylist.get(0).getId());
            intent.putExtra("playlistName", "Search Results");
            intent.putExtra("currentUser", FirebaseAuth.getInstance().getCurrentUser().getUid());
            intent.putExtra("songPosition", 0);
            startActivity(intent);
        };

        adapter = new SearchSongAdapter(getContext(), songs, onSearchItemClickListener);
        searchRecycler.setAdapter(adapter);
        new GetLatestHits().execute();

        return view;
    }

    private class GetLatestHits extends AsyncTask<Void, Void, List<Song>> {
        @Override
        protected List<Song> doInBackground(Void... voids) {
            List<Song> songs = fetchLatestHits();
            Log.d("TAG", "songs size: " + songs.size());
            return songs;
        }

        @Override
        protected void onPostExecute(List<Song> songs) {
            super.onPostExecute(songs);
            adapter.updateData(songs);
        }

        public List<Song> fetchLatestHits() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("songs");
            List<Song> songsList = new ArrayList<>();

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Song song = snapshot1.getValue(Song.class);
                        songsList.add(song);
                        Log.d("TAG", "\nAdded Song to allSongs: " + song.getTitle());
                    }
                    allSongs.clear();
                    allSongs.addAll(songsList);
//                    adapter.notifyDataSetChanged();
                    Log.d("TAG", "allSongs size: " + songsList.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("TAG", "Songs not loaded into the List");
                }
            });
            Log.d("TAG", "allSongs size: " + songsList.size());
            return songsList;
        }
    }

    public void searchSongs(final String keyword) {
        Log.d("TAG", "entered searchSongs()...");
        Log.d("TAG", "5.Search Recycler Visibility: " + searchRecycler.getVisibility());
//        searchRecycler.setVisibility(View.VISIBLE);
        cancelTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (keyword.trim().isEmpty()) {
                    songs = allSongs;
                } else {
                    ArrayList<Song> searchResult = new ArrayList<>();
                    for (Song song : allSongs) {
                        // Perform null checks before calling toLowerCase()
                        String title = song.getTitle() != null ? song.getTitle().toLowerCase() : "";
                        String artist = song.getArtist() != null ? song.getArtist().toLowerCase() : "";
                        String album = song.getAlbum() != null ? song.getAlbum().toLowerCase() : "";

                        Log.d("TAG", "title: " + title);
                        Log.d("TAG", "artist: " + artist);
                        Log.d("TAG", "album: " + album);

                        Log.d("TAG", "---------------ENDS---------------");

                        if (title.contains(keyword.toLowerCase())
                                || artist.contains(keyword.toLowerCase())
                                || album.contains(keyword.toLowerCase())) {
                            Log.d("TAG", "title: " + title);
                            Log.d("TAG", "artist: " + artist);
                            Log.d("TAG", "album: " + album);
                            searchResult.add(song);
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("TAG", "updating UI...");
//                            searchRecycler.setVisibility(View.VISIBLE);
                            songs.clear();
                            songs.addAll(searchResult);
                            Log.d("TAG", "6.Search Recycler Visibility: " + searchRecycler.getVisibility());
                            Log.d("TAG", "6.songs size: " + songs.size());
                            Log.d("TAG", "Fetched Search Results...");
                            for (Song song : songs) {
                                Log.d("TAG", "Song: " + song.getTitle());
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        adapter.notifyDataSetChanged();
//                    }
//                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null)
            timer.cancel();
    }

//    public void updateData(List<Song> newList) {
//        this.allSongs = newList;
//        adapter.notifyDataSetChanged();
//    }
}