package com.example.musix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musix.R;
import com.example.musix.models.Playlist;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {
    List<Playlist> playlists = new ArrayList<>();

    public PlaylistsAdapter(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.playlists_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.banner.setImageResource(playlists.get(position).getBanner());
        holder.title.setText(playlists.get(position).getTitle());
        holder.artist.setText(playlists.get(position).getCreator());
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, artist;
        RoundedImageView banner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.songBanner);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
        }
    }
}
