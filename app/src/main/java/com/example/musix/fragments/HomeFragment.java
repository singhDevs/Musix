package com.example.musix.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    RecyclerView latestHitsRecycler, playlistsRecycler, languageRecycler;
    List<Song> latestHitsList = new ArrayList<>();
    List<Playlist> playlists = new ArrayList<>();
    List<Playlist> songsByLanguageList = new ArrayList<>();
    Button uploadSongs;
    LatestHitsAdapter latestHitsAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Latest Hits Recycler
        latestHitsRecycler = view.findViewById(R.id.recyclerLatestHits);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        latestHitsRecycler.setLayoutManager(layoutManager);
        LatestHitsAdapter.OnSongClickListener onSongClickListener = new LatestHitsAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song) {
                Intent intent = new Intent(getContext(), MusicPlayer.class);
                intent.putExtra("song", song);
                intent.putExtra("songUrl", song.getId());
                startActivity(intent);
            }
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


//    public CompletableFuture<List<Song>> getLatestHits(){
//        Log.d("TAG", "entered Completable Future");
//        CompletableFuture<List<Song>> future = new CompletableFuture<>();
//        try{
//            List<Song> songList = fetchLatestHits();
//            future.complete(songList);
//        }
//        catch (Exception e){
//            future.completeExceptionally(e);
//        }
//
//        return future;
//    }

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
//            latestHitsList = songs;
//            Log.d("TAG", "size: " + latestHitsList.size());
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