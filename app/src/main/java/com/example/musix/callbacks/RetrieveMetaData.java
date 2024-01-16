package com.example.musix.callbacks;

import com.example.musix.models.Song;

public interface RetrieveMetaData {
    void onMetaDataFetched(Song song);
}
