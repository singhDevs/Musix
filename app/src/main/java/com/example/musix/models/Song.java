package com.example.musix.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Parcelable, Serializable {
    private String id;
    private String Banner;
    private String title;
    private String artist;
    private int durationInSeconds;
    private String album;
    private int releaseYear;

    public Song(String id, String banner, String title, String artist, int durationInSeconds, String album, int releaseYear) {
        this.id = id;
        this.Banner = banner;
        this.title = title;
        this.artist = artist;
        this.durationInSeconds = durationInSeconds;
        this.album = album;
        this.releaseYear = releaseYear;
    }

    public Song(String id, String banner, String title, String artist) {
        this.id = id;
        this.Banner = banner;
        this.title = title;
        this.artist = artist;
    }

    // Parcelable constructor
    protected Song(Parcel in) {
        id = in.readString();
        Banner = in.readString();
        title = in.readString();
        artist = in.readString();
        durationInSeconds = in.readInt();
        album = in.readString();
        releaseYear = in.readInt();
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
        dest.writeString(Banner);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeInt(durationInSeconds);
        dest.writeString(album);
        dest.writeInt(releaseYear);
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
}
