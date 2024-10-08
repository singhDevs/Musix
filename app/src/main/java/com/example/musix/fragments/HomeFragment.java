package com.example.musix.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.activities.NewMusicPlayer;
import com.example.musix.activities.PlaylistActivity;
import com.example.musix.adapters.LanguageAdapter;
import com.example.musix.adapters.LatestHitsAdapter;
import com.example.musix.adapters.PlaylistsAdapter;
import com.example.musix.callbacks.PlaylistCallback;
import com.example.musix.dialogs.ViewDialog;
import com.example.musix.handlers.FirebaseHandler;
import com.example.musix.handlers.GoogleSignInHelper;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    RecyclerView latestHitsRecycler, playlistsRecycler, languageRecycler;
    List<Song> latestHitsList = new ArrayList<>(), songsList = new ArrayList<>();
    List<Playlist> playlists = new ArrayList<>();
    List<Playlist> songsByLanguageList = new ArrayList<>();
    LatestHitsAdapter latestHitsAdapter;
    PlaylistsAdapter playlistsAdapter;
    LanguageAdapter languageAdapter;
    TextView greetingTxt, latestHitsSeeAll;
    ImageView photo;
    GoogleSignInOptions gso;
    ViewDialog viewDialog;
    SwipeRefreshLayout swipeRefreshLayout;
    private Button uploadSong;
    private GoogleSignInAccount account;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        uploadSong = view.findViewById(R.id.uploadSong);
        uploadSong.setOnClickListener(v -> uploadSong());

        account = GoogleSignIn.getLastSignedInAccount(getContext());
        gso = GoogleSignInHelper.getSignInOptions(getContext());

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            new GetLatestHits().execute();
            new GetPlaylistTask(playlists -> {}).execute();
            new GetLangPlaylistTask(playlists -> {}).execute();
        });


        latestHitsSeeAll = view.findViewById(R.id.latestHitsSeeAll);
        latestHitsSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Boolean> songsMap = new HashMap<>();
                int duration = 0;
                for (Song song : songsList) {
                    songsMap.put(song.getKey(), true);
                    duration += song.getDurationInSeconds();
                }

                Playlist playlist = new Playlist("Latest Hits", "Musix", duration, songsMap);
                Intent intent = new Intent(getContext(), PlaylistActivity.class);
                intent.putExtra("playlist", (Parcelable) playlist);
//                if(playlists == null) Log.d("TAG", "in Home Fragment, playlist is null!");
//                else {
//                    if(playlists.getSongs() == null){
//                        Log.d("TAG", "in Home Fragment, playlist songs is NULL!");
//                        playlists.setSongs(new HashMap<>());
//                    }
//                    else{
//                        Log.d("TAG", "in Home Fragment, playlist is NOT null! size: " + playlists.getSongs().size());
//                        for(Map.Entry<String, Boolean> entry : playlists.getSongs().entrySet()){
//                            Log.d("TAG", "key: " + entry.getKey() + "\t\tValue: " + entry.getValue());
//                        }
//                    }
//                }
                startActivity(intent);
            }
        });

        String greeting = getGreeting();
        greetingTxt = view.findViewById(R.id.greetingTxt);
        greetingTxt.setText(greeting);

        viewDialog = new ViewDialog(getContext(), getActivity());
        photo = view.findViewById(R.id.photo);
//        String name = "", email = "", uri = "";
//        if(account == null){
//            name = account.getEmail();
//            uri = "android.resource://" + requireContext().getPackageName() + "/drawable/user";
//        }
//        else{
//            name = account.getDisplayName();
//            uri = account.getPhotoUrl().toString();
//        }
//        email = account.getEmail();
        photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = "", email = "", uri = "";
                if(account == null){
                    name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    uri = "android.resource://" + requireContext().getPackageName() + "/drawable/user";
                }
                else{
                    name = account.getDisplayName();
                    uri = account.getPhotoUrl().toString();
                    email = account.getEmail();
                }
                viewDialog.showAccountDialog(name, email, uri);
            }
        });

        if(account != null){
            Glide.with(getContext())
                    .load(account.getPhotoUrl())
                    .circleCrop()
                    .into(photo);
        }
        else{
            Glide.with(getContext())
                    .load(R.drawable.user)
                    .circleCrop()
                    .into(photo);
        }

        //Latest Hits Recycler
        latestHitsRecycler = view.findViewById(R.id.recyclerLatestHits);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        latestHitsRecycler.setLayoutManager(layoutManager);
        LatestHitsAdapter.OnSongClickListener onSongClickListener = (song, position) -> {
            Intent intent = new Intent(getContext(), NewMusicPlayer.class);
            intent.putExtra("source", "internet");
            intent.putExtra("song", (Parcelable) song);
            intent.putExtra("songUrl", song.getId());
            intent.putExtra("songList", (Serializable) latestHitsList);
            intent.putExtra("playlistName", "Latest Hits");
            intent.putExtra("currentUser", FirebaseAuth.getInstance().getCurrentUser().getUid());
            Log.d("TAG", "SongList size: " + (latestHitsList != null ? latestHitsList.size() : 0));
            intent.putExtra("songPosition", position);
            startActivity(intent);
        };
        latestHitsAdapter = new LatestHitsAdapter(new ArrayList<>(), onSongClickListener);
        latestHitsRecycler.setAdapter(latestHitsAdapter);
        new GetLatestHits().execute();



        //Playlist Recycler
//        playlists.add(new Playlist(R.drawable.img, "Best of Diljit Dosanjh's", "Musix", 9000, new HashMap<>()));
        playlistsRecycler = view.findViewById(R.id.recyclerPlaylists);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        playlistsRecycler.setLayoutManager(layoutManager1);
        PlaylistsAdapter.OnPlaylistClicked onPlaylistClicked = playlist -> {
            Intent intent = new Intent(getContext(), PlaylistActivity.class);
            intent.putExtra("playlist", (Parcelable) playlist);
            if(playlist == null) Log.d("TAG", "in Home Fragment, playlist is null!");
            else {
                if(playlist.getSongs() == null){
                    Log.d("TAG", "in Home Fragment, playlist songs is NULL!");
                    playlist.setSongs(new HashMap<>());
                }
                else{
                    Log.d("TAG", "in Home Fragment, playlist is NOT null! size: " + playlist.getSongs().size());
                    for(Map.Entry<String, Boolean> entry : playlist.getSongs().entrySet()){
                        Log.d("TAG", "key: " + entry.getKey() + "\t\tValue: " + entry.getValue());
                    }
                }
            }
            startActivity(intent);
        };
        playlistsAdapter = new PlaylistsAdapter(getContext(), playlists, onPlaylistClicked);
        playlistsRecycler.setAdapter(playlistsAdapter);
        new GetPlaylistTask(playlists -> {
//                Log.d("TAG", "after fetching is done, title: " + playlists.get(0).getTitle());
//                Log.d("TAG", "after fetching is done, songMap: " + playlists.get(0).getSongs());
        }).execute();


        //Browse By Language Recycler
        languageRecycler = view.findViewById(R.id.recyclerLanguage);
        GridLayoutManager layoutManager2 = new GridLayoutManager(getContext(), 2);
        languageRecycler.setLayoutManager(layoutManager2);

//        songsByLanguageList.add(new Playlist(R.drawable.img, "Punjabi", "Musix", 9000, new HashMap<>()) );
//        languageRecycler = view.findViewById(R.id.recyclerLanguage);
//
//        GridLayoutManager layoutManager2 = new GridLayoutManager(getContext(), 3);
//        languageRecycler.setLayoutManager(layoutManager2);

        LanguageAdapter.OnPlaylistClicked onPlaylistClicked1 = playlist -> {
            Intent intent = new Intent(getContext(), PlaylistActivity.class);
            intent.putExtra("playlist", (Parcelable) playlist);
            if(playlist == null) Log.d("TAG", "in Home Fragment, playlist is null!");
            else {
                if(playlist.getSongs() == null){
                    Log.d("TAG", "in Home Fragment, playlist songs is NULL!");
                    playlist.setSongs(new HashMap<>());
                }
                else{
                    Log.d("TAG", "in Home Fragment, playlist is NOT null! size: " + playlist.getSongs().size());
                    for(Map.Entry<String, Boolean> entry : playlist.getSongs().entrySet()){
                        Log.d("TAG", "key: " + entry.getKey() + "\t\tValue: " + entry.getValue());
                    }
                }
            }
            startActivity(intent);
        };
        languageAdapter = new LanguageAdapter(getContext(), songsByLanguageList, onPlaylistClicked1);

        languageRecycler.setAdapter(languageAdapter);
        new GetLangPlaylistTask(playlists -> {
//                Log.d("TAG", "after fetching is done, title: " + playlists.get(0).getTitle());
//                Log.d("TAG", "after fetching is done, songMap: " + playlists.get(0).getSongs());
        }).execute();

        return view;
    }

    private String getGreeting() {
        int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        if(account != null) {
            if (hour >= 5 && hour < 12) {
                return "Good morning, " + account.getDisplayName();
            } else if (hour >= 12 && hour < 18) {
                return "Good afternoon, " + account.getDisplayName();
            } else {
                return "Good evening, " + account.getDisplayName();
            }
        }
        else{
            if (hour >= 5 && hour < 12) {
                return "Good morning!";
            } else if (hour >= 12 && hour < 18) {
                return "Good afternoon!";
            } else {
                return "Good evening!";
            }
        }
    }

    public void uploadSong() {
        Song song = new Song("", "", "", "In the End", "Linkin Park", 216, "Hybrid Theory", 2000, "english", "");
        int uri = getContext().getResources().getIdentifier("linkinpark", "raw", getContext().getPackageName());
        int bannerUri = getContext().getResources().getIdentifier("linkinpark_bg", "raw", getContext().getPackageName());

        Log.d("TAG", "Song File URI: " + uri);
        FirebaseHandler.uploadSongData(song, uri, bannerUri, getContext());
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

    private class GetPlaylistTask extends AsyncTask<Void, Void, List<Playlist>>{
        private final PlaylistCallback playlistCallback;

        private GetPlaylistTask(PlaylistCallback playlistCallback) {
            this.playlistCallback = playlistCallback;
        }

        @Override
        protected List<Playlist> doInBackground(Void... voids) {return fetchPlaylists();}
        private List<Playlist> fetchPlaylists() {
            Log.d("TAG", "entered fetchPlaylists");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("playlist")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            List<Playlist> playlistList = new ArrayList<>();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Playlist playlist = dataSnapshot.getValue(Playlist.class);
                        Log.d("playlist", "playlist title: " + playlist.getTitle());
                        playlistList.add(playlist);
                    }
                    playlists.clear();
                    playlists.addAll(playlistList);
                    playlistsAdapter.notifyDataSetChanged();
                    Log.d("TAG", "Fetched Playlists");
                    for(Playlist playlist : playlists){
                        Log.d("TAG", "title: " + playlist.getTitle());
                        Log.d("TAG", "banner: " + playlist.getBanner());
                        Log.d("TAG", "creator: " + playlist.getCreator());
                        Log.d("TAG", "duration: " + playlist.getDuration());
                        Log.d("TAG", "songMap: " + playlist.getSongs());
                    }
                    playlistCallback.onPlaylistsFetched(playlistList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "Error fetching data: " + error.getMessage());
                }
            });
            return playlistList;
        }
    }

    private class GetLangPlaylistTask extends AsyncTask<Void, Void, List<Playlist>>{
        private final PlaylistCallback playlistCallback;

        private GetLangPlaylistTask(PlaylistCallback playlistCallback) {
            this.playlistCallback = playlistCallback;
        }

        @Override
        protected List<Playlist> doInBackground(Void... voids) {return fetchLangPlaylists();}
        private List<Playlist> fetchLangPlaylists() {
            Log.d("DB", "entered fetchPlaylists");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("musix")
                    .child("language");
            List<Playlist> playlistList = new ArrayList<>();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Playlist playlist = dataSnapshot.getValue(Playlist.class);
                        Log.d("playlist", "playlist title: " + playlist.getTitle());
                        playlistList.add(playlist);
                    }
                    songsByLanguageList.clear();
                    songsByLanguageList.addAll(playlistList);
                    languageAdapter.notifyDataSetChanged();

                    Log.d("TAG", "Fetched Playlists");
                    for(Playlist playlist : songsByLanguageList){
                        Log.d("TAG", "title: " + playlist.getTitle());
                        Log.d("TAG", "banner: " + playlist.getBanner());
                        Log.d("TAG", "creator: " + playlist.getCreator());
                        Log.d("TAG", "duration: " + playlist.getDuration());
                        Log.d("TAG", "songMap: " + playlist.getSongs());
                    }
                    playlistCallback.onPlaylistsFetched(playlistList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "Error fetching data: " + error.getMessage());
                }
            });
            swipeRefreshLayout.setRefreshing(false);
            return playlistList;
        }
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(getContext(), gso).signOut();
    }
}