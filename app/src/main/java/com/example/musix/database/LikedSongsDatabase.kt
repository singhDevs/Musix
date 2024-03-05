package com.example.musix.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.musix.models.Song

@Database(entities = [Song::class], version = 1, exportSchema = false)
abstract class LikedSongsDatabase: RoomDatabase() {
    abstract fun songDao(): LikedSongDAO
    companion object{
        private const val DATABASE_NAME = "liked_songs.db"
        private var instance: LikedSongsDatabase? = null

        fun getInstance(context: Context): LikedSongsDatabase{
            if(instance == null){
                synchronized(LikedSongsDatabase::class.java){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LikedSongsDatabase::class.java,
                        DATABASE_NAME)
                        .build()
                }
            }
            return instance!!
        }
    }
}