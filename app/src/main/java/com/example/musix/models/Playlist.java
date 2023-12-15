package com.example.musix.models;

public class Playlist {
    private int Banner;
    private String title;
    private String creator;
    private int durationInSeconds;

    public Playlist(int banner, String title, String creator, int durationInSeconds) {
        Banner = banner;
        this.title = title;
        this.creator = creator;
        this.durationInSeconds = durationInSeconds;
    }

    public int getBanner() {
        return Banner;
    }

    public void setBanner(int banner) {
        Banner = banner;
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

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }
}
