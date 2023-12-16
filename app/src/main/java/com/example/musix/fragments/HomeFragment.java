package com.example.musix.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musix.R;
import com.example.musix.activities.MusicPlayer;
import com.example.musix.adapters.LatestHitsAdapter;
import com.example.musix.adapters.PlaylistsAdapter;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends Fragment {
    RecyclerView latestHitsRecycler, playlistsRecycler, languageRecycler;
    List<Song> latestHitsList = new ArrayList<>();
    List<Playlist> playlists = new ArrayList<>();
    List<Playlist> songsByLanguageList = new ArrayList<>();
    LatestHitsAdapter latestHitsAdapter;
    TextView greetingTxt;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        String greeting = getGreeting();
        greetingTxt = view.findViewById(R.id.greetingTxt);
        greetingTxt.setText(greeting);


        //Latest Hits Recycler
        latestHitsRecycler = view.findViewById(R.id.recyclerLatestHits);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        latestHitsRecycler.setLayoutManager(layoutManager);
        LatestHitsAdapter.OnSongClickListener onSongClickListener = (song, position) -> {
            Intent intent = new Intent(getContext(), MusicPlayer.class);
            intent.putExtra("song", (Parcelable) song);
            intent.putExtra("songUrl", song.getId());
            intent.putExtra("songList", (Serializable) latestHitsList);
            Log.d("TAG", "SongList size: " + (latestHitsList != null ? latestHitsList.size() : 0));
            intent.putExtra("songPosition", position);
            startActivity(intent);
        };
        latestHitsAdapter = new LatestHitsAdapter(new ArrayList<>(), onSongClickListener);
        latestHitsRecycler.setAdapter(latestHitsAdapter);
        new GetLatestHits().execute();



        //Playlist Recycler
        playlists.add(new Playlist(R.drawable.img, "Best of Diljit Dosanjh's", "Musix", 9000));
        playlistsRecycler = view.findViewById(R.id.recyclerPlaylists);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        playlistsRecycler.setLayoutManager(layoutManager1);

        PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(playlists);
        playlistsRecycler.setAdapter(playlistsAdapter);


        //Browse By Language Recycler
        songsByLanguageList.add(new Playlist(R.drawable.img, "Punjabi", "Musix", 9000));
        languageRecycler = view.findViewById(R.id.recyclerLanguage);

        GridLayoutManager layoutManager2 = new GridLayoutManager(getContext(), 3);
        languageRecycler.setLayoutManager(layoutManager2);

        PlaylistsAdapter languageAdapter = new PlaylistsAdapter(songsByLanguageList);
        languageRecycler.setAdapter(languageAdapter);

        return view;
    }

    private String getGreeting() {
        int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        if (hour >= 5 && hour < 12) {
            return "Good morning!";
        }
        else if (hour >= 12 && hour < 18) {
            return "Good afternoon!";
        }
        else {
            return "Good evening!";
        }
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
            latestHitsAdapter.updateData(songs);
        }
    }

    public List<Song> fetchLatestHits(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("songs");
        List<Song> songsList = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Song song = snapshot1.getValue(Song.class);
                    songsList.add(song);
                    Log.d("TAG", "\nAdded Song: " + song.getTitle());
                }
                latestHitsList.clear();
                latestHitsList.addAll(songsList);
                latestHitsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Songs not loaded into the List");
            }
        });
        Log.d("TAG", "songsList size: " + songsList.size());
        return songsList;
    }
}