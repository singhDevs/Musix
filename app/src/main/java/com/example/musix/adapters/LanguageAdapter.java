package com.example.musix.adapters;

import android.content.Context;
import android.util.Log;
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

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    List<Playlist> playlists;
    LanguageAdapter.OnPlaylistClicked onPlaylistClicked;
    Context context;

    public LanguageAdapter(Context context, List<Playlist> playlists, LanguageAdapter.OnPlaylistClicked onPlaylistClicked) {
        this.playlists = playlists;
        this.onPlaylistClicked = onPlaylistClicked;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.language_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int resourceId = context.getResources().getIdentifier("bg_playlist", "drawable", context.getPackageName());
        Log.d("TAG", "" + playlists.get(position).getBanner());

        Glide.with(holder.itemView.getContext())
                .load(playlists.get(position).getBanner())
                .into(holder.banner);
        holder.title.setText(playlists.get(position).getTitle());

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
        TextView title;
        RoundedImageView banner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.banner);
            title = itemView.findViewById(R.id.langName);
        }
    }

    public interface OnPlaylistClicked{
        public void onPlaylistClicked(Playlist playlist);
    }
}
