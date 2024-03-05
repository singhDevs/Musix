package com.example.musix.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.musix.models.Song;

import java.util.List;

@Dao
public interface LikedSongDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSong(Song song);

    @Query("DELETE FROM liked_songs WHERE `key` = :key")
    void removeSong(String key);

    @Query("SELECT * FROM liked_songs")
    List<Song> getLikedSongs();

    @Query("DELETE FROM liked_songs")
    void deleteAllLikedSongs();

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE `key` = :key)")
    boolean isSongLiked(String key);
}
