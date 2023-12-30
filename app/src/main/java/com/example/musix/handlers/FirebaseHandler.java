package com.example.musix.handlers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.musix.adapters.PlaylistActivityAdapter;
import com.example.musix.callbacks.AddToPlaylistCallback;
import com.example.musix.callbacks.PlaylistCallback;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Objects;

public class FirebaseHandler {
    public static Task<Void> uploadSongData(Song song, Context context){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference user = databaseReference.child("songs");

        DatabaseReference newUser = user.push();
        String newUserId =newUser.getKey();
        song.setId(newUserId);

        return newUser.setValue(song).continueWithTask(task -> {
            if(!task.isSuccessful()){
                throw task.getException();
            }
            else{
                Toast.makeText(context, "Song uploaded", Toast.LENGTH_LONG).show();
            }
            return Tasks.forResult(null);
        });
    }

    public static Task<Uri> uploadSong(InputStream inputStream, Context context){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("songs/" + System.currentTimeMillis() + ".mp3");

        UploadTask uploadTask = storageReference.putStream(inputStream);
        return uploadTask.continueWithTask(task -> {
            if(!task.isSuccessful()){
                Toast.makeText(context, "Error in uploading Audio File", Toast.LENGTH_SHORT).show();
                throw task.getException();
            }
            Log.d("TAG", "song file uploaded!");
            return storageReference.getDownloadUrl();
        });
    }

    public static Task<Uri> uploadBanner(InputStream inputStream, Context context, String songUrl){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child("banner/" + songUrl + ".jpg");

        UploadTask uploadTask = reference.putStream(inputStream);
        return uploadTask.continueWithTask(task -> {
            if(!task.isSuccessful()){
                Toast.makeText(context, "Error in uploading Banner", Toast.LENGTH_SHORT).show();
                throw  task.getException();
            }
            Log.d("TAG", "banner file uploaded!");
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
        final Boolean[] isPresent = {false};
        databaseReference.child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.getKey().equals(song.getKey())){
                        Toast.makeText(context, "Song already present in this Playlist", Toast.LENGTH_SHORT).show();
                        isPresent[0] = true;
                    }
                    else{
                        databaseReference.child("duration").setValue(song.getDurationInSeconds());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Error in addListener: " + error.getMessage());
            }
        });

        if(!isPresent[0]){
            databaseReference.child("songs").child(song.getKey()).setValue(true).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(context, "Added to " + playlistName, Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(addToPlaylistCallback::OnSongAddedToPlaylist, 500);
                }
                else{
                    Log.d("TAG", "error: Key not set in DB. Error: " + task.getException());
                }
            });
        }
    }
}
