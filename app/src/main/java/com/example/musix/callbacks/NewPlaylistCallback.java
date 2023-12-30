package com.example.musix.callbacks;

import com.google.firebase.database.DatabaseReference;

public interface NewPlaylistCallback {
    public void onNewPlaylistCreated(DatabaseReference databaseReference);
}
