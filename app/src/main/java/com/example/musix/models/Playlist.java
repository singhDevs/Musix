package com.example.musix.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Playlist implements Parcelable {
    private String banner;
    private String title;
    private String creator;
    private int duration;
    private Map<String, Boolean> songs;

    public Playlist() {
    }

    public Playlist(String title, String creator, int duration, Map<String, Boolean> songs) {
        this.title = title;
        this.creator = creator;
        this.duration = duration;
        this.songs = songs;
    }

    public Playlist(String banner, String title, String creator, int duration, Map<String, Boolean> songs) {
        this.banner = banner;
        this.title = title;
        this.creator = creator;
        this.duration = duration;
        this.songs = songs;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Map<String, Boolean> getSongs() {
        return songs;
    }

    public void setSongs(HashMap<String, Boolean> songs) {
        this.songs = songs;
    }

    // Parcelable implementation
    protected Playlist(Parcel in) {
        banner = in.readString();
        title = in.readString();
        creator = in.readString();
        duration = in.readInt();

        int size = in.readInt();
        songs = new HashMap<>(size);
        for(int i = 0; i < size; i++){
            String key = in.readString();
            boolean value = in.readByte() != 0;
            songs.put(key, value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(banner);
        dest.writeString(title);
        dest.writeString(creator);
        dest.writeInt(duration);

        dest.writeInt(songs.size());
        for(Map.Entry<String, Boolean> entry : songs.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeByte((byte) (entry.getValue() ? 1 : 0));
        }
        Log.d("TAG", "Entered writeToParcel");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }
        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
}
