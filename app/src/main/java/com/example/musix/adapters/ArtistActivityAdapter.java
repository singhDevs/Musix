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

public class ArtistActivityAdapter extends RecyclerView.Adapter<ArtistActivityAdapter.ViewHolder>{
    private List<Song> songList;
    private ArtistActivityAdapter.OnArtistSongClickListener onArtistSongClickListener;
    Context context;

    public ArtistActivityAdapter(Context context, List<Song> songList, ArtistActivityAdapter.OnArtistSongClickListener onArtistSongClickListener) {
        this.songList = songList;
        this.onArtistSongClickListener = onArtistSongClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistActivityAdapter.ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.playlist_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.number.setText(String.valueOf(position + 1));
        Glide.with(holder.itemView.getContext())
                .load(songList.get(position).getBanner())
                .into(holder.banner);
        holder.title.setText(songList.get(position).getTitle());
        holder.artist.setText(songList.get(position).getArtist());
        holder.duration.setText(formatTime(songList.get(position).getDurationInSeconds()));

        holder.itemView.setOnClickListener(view -> {
            if(onArtistSongClickListener != null){
                onArtistSongClickListener.onSongClicked(songList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
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

    public interface OnArtistSongClickListener{
        void onSongClicked(List<Song> songList, int position);
    }
}
