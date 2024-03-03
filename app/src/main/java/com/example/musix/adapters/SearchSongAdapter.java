package com.example.musix.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musix.models.Song;

import java.util.List;
import java.util.Timer;

public class SearchSongAdapter extends RecyclerView.Adapter<SearchSongAdapter.ViewHolder> {
    List<Song> songs, allSongs;
    Timer timer;
    Context context;
    OnSearchItemClickListener onSearchItemClickListener;

    public SearchSongAdapter(Context context, List<Song> songs, OnSearchItemClickListener onSearchItemClickListener) {
        this.songs = songs;
        this.context = context;
        this.onSearchItemClickListener = onSearchItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(songs.get(position).getBanner())
                .into(holder.banner);
        holder.title.setText(songs.get(position).getTitle());
        holder.artist.setText(songs.get(position).getArtist());
        holder.duration.setText(formatTime(songs.get(position).getDurationInSeconds()));

        holder.itemView.setOnClickListener(v -> {
            if(onSearchItemClickListener != null){
                onSearchItemClickListener.OnSongClicked(songs, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView number, title, artist, duration;
        ImageView banner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.banner);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            duration = itemView.findViewById(R.id.duration);
        }
    }



    public void cancelTimer() {
        if (timer != null)
            timer.cancel();
    }

    public void updateData(List<Song> newList){
        this.allSongs = newList;
        notifyDataSetChanged();
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public interface OnSearchItemClickListener{
        void OnSongClicked(List<Song> songs, int position);
    }
}

