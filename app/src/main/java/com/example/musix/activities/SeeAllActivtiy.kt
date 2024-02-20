package com.example.musix.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musix.R
import com.example.musix.models.Song

class SeeAllActivtiy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_all_activtiy)

        fetchIntentData()
        setUpUI()
    }

    private fun fetchIntentData(){
        val intent = intent
        val songList = intent.getSerializableExtra("songList") as List<Song?>?
        val playlistName = intent.getStringExtra("playlistName")
        val desc = intent.getStringExtra("description")
    }

    private fun setUpUI() {

    }

}