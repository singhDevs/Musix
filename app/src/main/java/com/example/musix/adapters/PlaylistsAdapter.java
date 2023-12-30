package com.example.musix.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.models.Playlist;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {
    List<Playlist> playlists;
    OnPlaylistClicked onPlaylistClicked;
    Context context;

    public PlaylistsAdapter(Context context, List<Playlist> playlists, OnPlaylistClicked onPlaylistClicked) {
        this.playlists = playlists;
        this.onPlaylistClicked = onPlaylistClicked;
        this.context = context;
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
        if(playlists.get(position).getTitle().equals("Liked Songs")){
            int resourceId = context.getResources().getIdentifier("bg_liked_playlist", "drawable", context.getPackageName());
            Glide.with(holder.itemView.getContext())
                    .load(resourceId)
                    .into(holder.banner);
        }
        else{
            int resourceId = context.getResources().getIdentifier("bg_playlist", "drawable", context.getPackageName());
            Glide.with(holder.itemView.getContext())
                    .load(resourceId)
                    .into(holder.banner);
        }
        holder.banner.setImageResource(playlists.get(position).getBanner());
        holder.title.setText(playlists.get(position).getTitle());
        holder.artist.setText(playlists.get(position).getCreator());

        holder.itemView.setOnClickListener(view -> {
            if(onPlaylistClicked != null){
                onPlaylistClicked.onPlaylistClicked(playlists.get(position));
            }
        });
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

    public interface OnPlaylistClicked{
        public void onPlaylistClicked(Playlist playlist);
    }
}
