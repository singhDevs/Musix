package com.example.musix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        int adapterPosition = holder.getBindingAdapterPosition();
        Glide.with(holder.itemView.getContext())
                .load(latestHitsList.get(adapterPosition).getBanner())
                .into(holder.banner);
        holder.title.setText(latestHitsList.get(adapterPosition).getTitle());
        holder.artist.setText(latestHitsList.get(adapterPosition).getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSongClickListener != null){
                    onSongClickListener.onSongClick(latestHitsList.get(adapterPosition), adapterPosition);
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
        void onSongClick(Song song, int position);
    }

    public void updateData(List<Song> newList){
        this.latestHitsList = newList;
        notifyDataSetChanged();
    }
}
