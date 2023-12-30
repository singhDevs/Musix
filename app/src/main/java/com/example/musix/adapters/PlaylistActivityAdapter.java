package com.example.musix.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.models.Song;

import java.util.List;

public class PlaylistActivityAdapter extends RecyclerView.Adapter<PlaylistActivityAdapter.ViewHolder> {
    private List<Song> playList; //TODO: should be list of playlists
    private OnPlaylistClickListener onPlaylistClickListener;
    Context context;

    public PlaylistActivityAdapter(Context context, List<Song> playList, OnPlaylistClickListener onPlaylistClickListener) {
        this.playList = playList;
        this.onPlaylistClickListener = onPlaylistClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.playlist_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.number.setText(String.valueOf(position + 1));
        Glide.with(holder.itemView.getContext())
                .load(playList.get(position).getBanner())
                .into(holder.banner);
        holder.title.setText(playList.get(position).getTitle());
        holder.artist.setText(playList.get(position).getArtist());
        holder.duration.setText(formatTime(playList.get(position).getDurationInSeconds()));

        holder.itemView.setOnClickListener(view -> {
            if(onPlaylistClickListener != null){
                onPlaylistClickListener.onPlaylistClick(playList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView number, title, artist, duration;
        ImageView banner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            banner = itemView.findViewById(R.id.banner);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            duration = itemView.findViewById(R.id.duration);
        }
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public interface OnPlaylistClickListener{
        void onPlaylistClick(List<Song> songList, int position);
    }
}
