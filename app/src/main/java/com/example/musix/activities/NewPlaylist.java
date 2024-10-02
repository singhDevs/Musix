package com.example.musix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
    Song song;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView(R.layout.activity_new_playlist);

        EditText edtTextName = findViewById(R.id.edtTextName);
        Button doneBtn = findViewById(R.id.doneBtn);

        Intent intent = getIntent();
        song = intent.getParcelableExtra("song");

        if(song == null)
            Log.d("TAG", "WE GOT SONG NULL!!");
        else
            Log.d("TAG", "SONG AIN'T NULL!!");

        doneBtn.setOnClickListener(view -> {
            Log.d("TAG","Entered doneBtn click listener");
            playlistName = edtTextName.getText().toString();
            new CreatePlaylist(databaseReference -> {
                Log.d("TAG", "inside doneBtn callback, now calling addSongToPlaylist...");
                FirebaseHandler.addSongToPlaylist(this, databaseReference, playlistName, song,new AddToPlaylistCallback() {
                    @Override
                    public void OnAddedToPlaylist() {}

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
            Log.d("TAG", "in Background: createPlaylist");
            createNewPlaylist();
            return null;
        }

        private void createNewPlaylist() {
            Log.d("TAG", "Entered createNewPlaylist()");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("playlist")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            HashMap<String, Boolean> songHashMap = new HashMap<>();
            if(song == null) Log.d("TAG", "SONG is NULL!!");
            else{
                Log.d("TAG", "song Key: " + song.getKey());
                songHashMap.put(song.getKey(), true);
            }
            Playlist playlist = new Playlist("", playlistName, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), 0, songHashMap);

            Log.d("TAG", playlist.getSongs().toString());
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