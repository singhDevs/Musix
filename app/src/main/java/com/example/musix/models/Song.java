package com.example.musix.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "liked_songs")
public class Song implements Parcelable, Serializable {
    @PrimaryKey(autoGenerate = true)
    private int sno;
    private String key;
    private String id;
    private String Banner;
    private String title;
    private String artist;
    private int durationInSeconds;
    private String album;
    private int releaseYear;
    private String language;

    public Song(String id, String key, String banner, String title, String artist, int durationInSeconds, String album, int releaseYear, String language) {
        this.id = id;
        this.key = key;
        this.Banner = banner;
        this.title = title;
        this.artist = artist;
        this.durationInSeconds = durationInSeconds;
        this.album = album;
        this.releaseYear = releaseYear;
        this.language = language;
    }

    public Song(String id, String key, String banner, String title, String artist, String language) {
        this.id = id;
        this.key = key;
        this.Banner = banner;
        this.title = title;
        this.artist = artist;
        this.language = language;
    }

    // Parcelable constructor
    protected Song(Parcel in) {
        id = in.readString();
        key = in.readString();
        Banner = in.readString();
        title = in.readString();
        artist = in.readString();
        durationInSeconds = in.readInt();
        album = in.readString();
        releaseYear = in.readInt();
        language = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public Song() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(Banner);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeInt(durationInSeconds);
        dest.writeString(album);
        dest.writeInt(releaseYear);
        dest.writeString(language);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBanner() {
        return Banner;
    }

    public void setBanner(String banner) {
        Banner = banner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }
}
