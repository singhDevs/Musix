package com.example.musix.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Artist implements Parcelable {
    private String banner;
    private String title;
    private String text;
    private Map<String, Boolean> songs;

    public Artist() {
    }

    public Artist(String banner, String title, String text, Map<String, Boolean> songs) {
        this.banner = banner;
        this.title = title;
        this.text = text;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Boolean> getSongs() {
        return songs;
    }

    public void setSongs(Map<String, Boolean> songs) {
        this.songs = songs;
    }


    // Parcelable implementation
    protected Artist(Parcel in) {
        banner = in.readString();
        title = in.readString();
        text = in.readString();

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
        dest.writeString(text);

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

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }
        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}
