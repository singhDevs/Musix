package com.example.musix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.musix.R;
import com.example.musix.callbacks.AddToPlaylistCallback;
import com.example.musix.callbacks.NewPlaylistCallback;
import com.example.musix.handlers.FirebaseHandler;
import com.example.musix.models.Playlist;
import com.example.musix.models.Song;
import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class NewPlaylist extends AppCompatActivity {
    String playlistName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_playlist);

        EditText edtTextName = findViewById(R.id.edtTextName);
        Button doneBtn = findViewById(R.id.doneBtn);

        Intent intent = getIntent();
        Song song = intent.getParcelableExtra("song");

        doneBtn.setOnClickListener(view -> {
            Log.d("TAG","Entered doneBtn click listener");
            playlistName = edtTextName.getText().toString();
            new CreatePlaylist(databaseReference -> {
                Log.d("TAG", "inside doneBtn callback, now calling addSongToPlaylist...");
                FirebaseHandler.addSongToPlaylist(this, databaseReference, playlistName, song, new AddToPlaylistCallback() {
                    @Override
                    public void OnAddedToPlaylist() {
                    }

                    @Override
                    public void OnSongAddedToPlaylist() {
                        finish();
                    }
                });
            }).execute();
        });
    }

    private class CreatePlaylist extends AsyncTask<Void, Void, Void>{
        private NewPlaylistCallback newPlaylistCallback;

        public CreatePlaylist(NewPlaylistCallback newPlaylistCallback) {
            this.newPlaylistCallback = newPlaylistCallback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            createNewPlaylist();
            return null;
        }

        private void createNewPlaylist() {
            Log.d("TAG", "Entered createNewPlaylist()");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("playlist")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            Playlist playlist = new Playlist(0, playlistName, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), 0, new HashMap<>());


            DatabaseReference newPlaylist = databaseReference.child(playlistName);
            newPlaylist.setValue(playlist).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d("TAG", "newPlaylist set complete. now calling callback...");
                        newPlaylistCallback.onNewPlaylistCreated(newPlaylist);
                    }
                    else{
                        Log.d("TAG", "Error in setting newPlaylist: " + task.getException());
                    }
                }
            });
        }
    }
}