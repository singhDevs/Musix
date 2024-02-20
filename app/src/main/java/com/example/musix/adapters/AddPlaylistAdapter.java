package com.example.musix.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musix.R;
import com.example.musix.models.Playlist;

import java.util.List;

public class AddPlaylistAdapter extends RecyclerView.Adapter<AddPlaylistAdapter.ViewHolder> {
    List<Playlist> playlists;
    AddPlaylistAdapter.OnPlaylistClicked onPlaylistClicked;

    public AddPlaylistAdapter(List<Playlist> playlists, AddPlaylistAdapter.OnPlaylistClicked onPlaylistClicked) {
        this.playlists = playlists;
        this.onPlaylistClicked = onPlaylistClicked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.add_to_playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(playlists != null && playlists.get(position).getTitle().equals("Liked Songs")){
            int resourceId = holder.itemView.getContext().getResources().getIdentifier("bg_liked_playlist", "drawable", holder.itemView.getContext().getPackageName());
            Glide.with(holder.itemView.getContext())
                    .load(resourceId)
                    .into(holder.banner);
        }
        else{
            int resourceId = holder.itemView.getContext().getResources().getIdentifier("bg_playlist", "drawable", holder.itemView.getContext().getPackageName());
            Glide.with(holder.itemView.getContext())
                    .load(resourceId)
                    .into(holder.banner);
        }
        holder.playlistTitle.setText(playlists.get(position).getTitle());

        String textNum = "";
        if(playlists.get(position).getSongs() != null) textNum = playlists.get(position).getSongs().size() + " song";
        if(playlists.get(position).getSongs() == null) {
            Log.d("TAG", "inside AddPlaylist, Songs is NULL!");
            holder.numTracks.setText("0 tracks");
        }
        else{
            if(playlists.get(position).getSongs().size() > 1) textNum += "s";
            holder.numTracks.setText(textNum);
        }

        holder.itemView.setOnClickListener(view -> {
            if(onPlaylistClicked != null){
                onPlaylistClicked.onPlaylistClicked(playlists.get(position), holder.itemView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView banner;
        TextView playlistTitle, numTracks;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.banner);
            playlistTitle = itemView.findViewById(R.id.playlistTitle);
            numTracks = itemView.findViewById(R.id.numTracks);
        }
    }

    public interface OnPlaylistClicked {
        void onPlaylistClicked(Playlist playlist, View view);
    }
}

