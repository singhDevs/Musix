package com.example.musix.callbacks;

import com.example.musix.models.Artist;
import com.example.musix.models.Song;

import java.util.List;

public interface RetrieveArtist {
    public void onArtistRetrieved(Artist artist);
    public void onArtistRetrievalFailed(String msg);
    public void onSongsFetched(List<Song> songs);
    public void onSongsFetchedError(String msg);
}
