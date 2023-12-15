package com.example.musix.handlers;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.example.musix.models.Song;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.internal.api.FirebaseNoSignedInUserException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

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


}
