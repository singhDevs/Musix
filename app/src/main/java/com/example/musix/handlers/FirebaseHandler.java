package com.example.musix.handlers;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.musix.callbacks.AddToPlaylistCallback;
import com.example.musix.callbacks.SongCheckCallback;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FirebaseHandler {
    static boolean isPresent;
    public static Task<Void> uploadSongData(Song song, int uri, int bannerUri, Context context){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference user = databaseReference.child("songs");

        DatabaseReference newUser = user.push();
        String newUserId =newUser.getKey();
        song.setId(newUserId);

        InputStream stream = context.getResources().openRawResource(uri);

        return newUser.setValue(song).continueWithTask(task -> {
            if(!task.isSuccessful()){
                throw task.getException();
            }
            else{
                Toast.makeText(context, "Song uploaded", Toast.LENGTH_LONG).show();
                Log.d("TAG", "Song Data uploaded, now uploading Song File...");
                uploadSong(song, stream, bannerUri, context);
            }
            return Tasks.forResult(null);
        });
    }

    public static Task<Uri> uploadSong(Song song, InputStream inputStream, int bannerUri, Context context){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("songs/" + System.currentTimeMillis() + ".mp3");

        UploadTask uploadTask = storageReference.putStream(inputStream);

        return uploadTask.continueWithTask(task -> {
            if (task.isSuccessful()) {
                Log.d("TAG", "Song file uploaded. Now uploading banner...");
                return storageReference.getDownloadUrl();
            } else {
                Log.e("TAG", "Error uploading song file", task.getException());
                throw task.getException();
            }
        }).continueWithTask(songUriTask -> {
            Uri songURI = songUriTask.getResult();
            InputStream stream = context.getResources().openRawResource(bannerUri);
            return uploadBanner(song, stream, context, storageReference.getDownloadUrl().toString());
        }).addOnSuccessListener(uri -> {
            Log.d("TAG", "Banner file uploaded successfully!");
        }).addOnFailureListener(e -> {
            Log.e("TAG", "Error uploading banner", e);
        });
    }

    public static Task<Uri> uploadBanner(Song song, InputStream inputStream, Context context, String songUrl){
        Log.d("TAG", "entered uploadBanner");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Log.d("TAG", "banner name: " + songUrl);
        StorageReference reference = storage.getReference().child("banner/" + songUrl + ".jpg");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("musix").child("language");

        UploadTask uploadTask = reference.putStream(inputStream);
        return uploadTask.continueWithTask(task -> {
            if(!task.isSuccessful()){
                Toast.makeText(context, "Error in uploading Banner", Toast.LENGTH_SHORT).show();
                throw  task.getException();
            }
            else{
                Log.d("TAG", "banner file uploaded!");
                Log.d("TAG", "song uploading to lang playlist...");
//                databaseReference.child(song.getLanguage()).child("songs").child("duration").setValue();
//                databaseReference.child(song.getLanguage()).child("songs").child(song.getKey()).setValue(true).addOnCompleteListener(task1 -> {
//                    if(task1.isSuccessful()){
//                        Log.d("TAG", "song uploaded to lang playlist!");
//                    }
//                    else{
//                        Log.e("TAG", "Error: " + task.getException());
//                    }
//                });
            }
            return reference.getDownloadUrl();
        });
    }

    public static void addLikedSong(Context context, Playlist playlist, String UID, Song song){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("playlist")
                .child(UID)
                .child("liked");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild("title")){
                    databaseReference.child("banner").setValue(playlist.getBanner());
                    databaseReference.child("title").setValue(playlist.getTitle());
                    databaseReference.child("duration").setValue(song.getDurationInSeconds());
                    databaseReference.child("creator").setValue(playlist.getCreator());
                }
                else{
                    if(snapshot.child("duration").getValue(Integer.class) != null){
                        databaseReference.child("duration").setValue(snapshot.child("duration").getValue(Integer.class) + song.getDurationInSeconds());
                    }
                    else{
                        Log.d("TAG", "duration is null");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        databaseReference.child("songs").child(song.getKey()).setValue(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("TAG", "error in Liked Song: ", task.getException());
            } else {
                Toast.makeText(context, "Added to Liked Songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void removeLikedSong(Context context, String UID, Song song) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("playlist")
                .child(UID)
                .child("liked");

        databaseReference.child("songs").child(song.getKey()).removeValue().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("TAG", "error in removing Liked Song: ", task.getException());
            } else {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        databaseReference.child("duration").setValue(snapshot.child("duration").getValue(Integer.class) - song.getDurationInSeconds());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(context, "Removed from Liked Songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static List<Playlist> getPlaylist(Context context, String UID){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("playlist")
                .child(UID);
        List<Playlist> playlists = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Playlist playlist = dataSnapshot.getValue(Playlist.class);
                    playlists.add(playlist);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return playlists;
    }

    public static void addSongToPlaylist(Context context, DatabaseReference databaseReference, String playlistName, Song song, AddToPlaylistCallback addToPlaylistCallback){          //reference to playlist node under UID
        Log.d("TAG", "inside addSongToPlaylist");
        isPresent = false;

        SongCheckCallback songCheckCallback = () -> {
            Log.d("TAG", "inside SongCheck callback");
            databaseReference.child("songs").child(song.getKey()).setValue(true).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            databaseReference.child("duration").setValue(snapshot.child("duration").getValue(Integer.class) + song.getDurationInSeconds());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    Toast.makeText(context, "Added to " + playlistName, Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(addToPlaylistCallback::OnSongAddedToPlaylist, 500);
                }
                else{
                    Log.d("TAG", "error: Key not set in DB. Error: " + task.getException());
                }
                });
        };

        databaseReference.child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("addSong", "No of children" + snapshot.getChildrenCount());
                if(snapshot.hasChildren()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.getKey().equals(song.getKey())) {
                            Log.d("TAG", "Song already present in this Playlist");
                            Toast.makeText(context, "Song already present in this Playlist", Toast.LENGTH_SHORT).show();
                            isPresent = true;
                        } else {
                            Log.d("TAG", "Adding to this Playlist");
                        }
                        songCheckCallback.OnSongChecked();
                    }
                }
                new Handler(Looper.getMainLooper()).postDelayed(addToPlaylistCallback::OnSongAddedToPlaylist, 500);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Error in addListener: " + error.getMessage());
            }
        });
    }
}