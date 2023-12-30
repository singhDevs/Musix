package com.example.musix.callbacks;

import com.example.musix.models.Playlist;

import java.util.List;

public interface PlaylistCallback {
    void onPlaylistsFetched(List<Playlist> playlists);
}
