package com.example.musix.fragments;

import static com.example.musix.handlers.FirebaseHandler.uploadBanner;
import static com.example.musix.handlers.FirebaseHandler.uploadSong;
import static com.example.musix.handlers.FirebaseHandler.uploadSongData;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    RecyclerView latestHitsRecycler, playlistsRecycler, languageRecycler;
    List<Song> latestHitsList = new ArrayList<>();
    List<Playlist> playlists = new ArrayList<>();
    List<Playlist> songsByLanguageList = new ArrayList<>();
    Button uploadSongs;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Latest Hits Recycler  
//        latestHitsList.add(new Song("1", R.drawable.img, "El Sueno", "Diljit Dosanjh"));
        latestHitsRecycler = view.findViewById(R.id.recyclerLatestHits);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        latestHitsRecycler.setLayoutManager(layoutManager);

//        uploadSongs = view.findViewById(R.id.uploadSongs);
//        uploadSongs.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                int resId = getResources().getIdentifier("heraudio", "raw", getContext().getPackageName());
//                int resId2 = getResources().getIdentifier("herbanner", "raw", getContext().getPackageName());
//
//                InputStream inputStream1 = getResources().openRawResource(resId);
//                InputStream inputStream2 = getResources().openRawResource(resId2);
//
//                String songURL = uploadSong(inputStream1, getContext()).toString();
//                String bannerURL = uploadBanner(inputStream2, getContext(), songURL).toString();
//                Song song = new Song();
//                song.setId(songURL);
//                song.setTitle("Her");
//                song.setArtist("Shubh");
//                song.setDurationInSeconds(154);
//                song.setReleaseYear(2022);
//                song.setBanner(bannerURL);
//                song.setAlbum("NA");
//
//                uploadSongData(song, getContext());
//            }
//        });

        LatestHitsAdapter.OnSongClickListener onSongClickListener = new LatestHitsAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song) {
                Intent intent = new Intent(getContext(), MusicPlayer.class);
                intent.putExtra("song", song);
                startActivity(intent);
            }
        };
        LatestHitsAdapter adapter = new LatestHitsAdapter(latestHitsList, onSongClickListener);
        latestHitsRecycler.setAdapter(adapter);




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
}