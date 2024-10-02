package com.example.musix.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.musix.R;
import com.example.musix.activities.NewMusicPlayer;
import com.example.musix.callbacks.RetrieveMetaData;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilesFragment extends Fragment{
    private static final int PICK_AUDIO_REQUEST_CODE = 1;
    MaterialButton playFromFilesBtn;
    private RetrieveMetaData retrieveMetaData;
    public FilesFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        playFromFilesBtn = view.findViewById(R.id.playFilesBtn);
        playFromFilesBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            startActivityForResult(intent, PICK_AUDIO_REQUEST_CODE);
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Song> songList = new ArrayList<>();
        if(requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Uri audioUri = data.getData();
//            String key = audioUri.toString();

            new FetchMetaDataTask(song -> {
                song.setId(String.valueOf(audioUri));
                songList.clear();
                songList.add(song);

                if(song == null) Log.d("local", "song1 is NULL");
                else Log.d("local", "song1 is not NULL");

                if(song.getId() != null) Log.d("local", " SONG URL: " + song.getId());
                else Log.d("local", "SONG URL IS NULL!");

                for(Song songItem : songList){
                    Log.d("local", "song title: " + songItem.getTitle());
                }

                Intent intent = new Intent(getContext(), NewMusicPlayer.class);
                intent.putExtra("song", (Parcelable) song);
                intent.putExtra("songUrl", song.getId());
                intent.putExtra("songList", (Serializable) songList);
                intent.putExtra("playlistName", "Files");
                intent.putExtra("currentUser", FirebaseAuth.getInstance().getCurrentUser().getUid());
                Log.d("TAG", "Files SongList size: " + songList.size());
                intent.putExtra("songPosition", 0);
                Log.d("local", "song from File: Title: " + song.getTitle() + "\tArtist: " + song.getArtist());
                startActivity(intent);
            }, audioUri).execute();
        }
        else{
            Toast.makeText(getContext(), "Failed to fetch Audio File", Toast.LENGTH_SHORT).show();
        }
    }

    private class FetchMetaDataTask extends AsyncTask<Void, Void, Song> {
        RetrieveMetaData retrieveMetaData;
        Uri uri;

        public FetchMetaDataTask(RetrieveMetaData retrieveMetaData, Uri uri) {
            this.retrieveMetaData = retrieveMetaData;
            this.uri = uri;
        }

//        private void getSongObject(Uri uri, RetrieveMetaData retrieveMetaData) {
//            Song song;
//
//            if(retrieveMetaData != null && song != null)
//                retrieveMetaData.onMetaDataFetched(song);
//        }
        @Override
        protected Song doInBackground(Void... Voids) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getContext(), uri);

            int drawableResourceId = R.drawable.bg_song_banner;
            Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(drawableResourceId)
                    + '/' + getResources().getResourceTypeName(drawableResourceId)
                    + '/' + getResources().getResourceEntryName(drawableResourceId));

            String id = uri.toString();
            String key = uri.toString();
            Log.d("TAG", "\n\nKEY IS: " + key + "\n\n");
            String banner = String.valueOf(drawableUri);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            int duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            int year = 0;
            String yearString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
            if(yearString != null && !yearString.isEmpty()){
                year = Integer.parseInt(yearString);
            }
            if(artist == null){
                artist = "Unknown Artist";
//                Log.d("TAG", "artist IS NULL, setting up author...");
//                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            }
            if(title == null){
                title = "Unknown Audio";
            }
            return new Song(id, key, banner, title, artist, duration, album, year, "", "");
        }

        @Override
        protected void onPostExecute(Song song) {
            super.onPostExecute(song);
            if(song == null) Log.d("TAG", "in OnPostExecute song is null!");
            else Log.d("TAG", "in OnPostExecute song is NOT null!");

            if(song.getTitle() == null) Log.d("TAG", "TITLE IS NULL");
            if(song.getArtist() == null){
                Log.d("TAG", "Artist IS NULL");
            }
            else{
                Log.d("TAG", "Author: " + song.getArtist());
            }
            if(song.getReleaseYear() == 0) Log.d("TAG", "YEAR IS ZERO");
            retrieveMetaData.onMetaDataFetched(song);
        }
    }

}