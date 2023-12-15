package com.example.musix.adapters;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musix.R;
import com.example.musix.models.Song;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class LatestHitsAdapter extends RecyclerView.Adapter<LatestHitsAdapter.ViewHolder> {
    private List<Song> latestHitsList;
    private OnSongClickListener onSongClickListener;

    public LatestHitsAdapter(List<Song> latestHitsList, OnSongClickListener onSongClickListener) {
        this.latestHitsList = latestHitsList;
        this.onSongClickListener = onSongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.latest_hits_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.banner.setImageResource(latestHitsList.get(position).getBanner());
        holder.title.setText(latestHitsList.get(position).getTitle());
        holder.artist.setText(latestHitsList.get(position).getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSongClickListener != null){
                    onSongClickListener.onSongClick(latestHitsList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return latestHitsList.size();
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

    public interface OnSongClickListener{
        void onSongClick(Song song);
    }
}
