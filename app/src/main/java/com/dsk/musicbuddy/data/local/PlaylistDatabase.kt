package com.dsk.musicbuddy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dsk.musicbuddy.data.model.Song

@Database(entities = [Song::class], version = 1, exportSchema = true)
abstract class MusicBuddyDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: MusicBuddyDatabase? = null

        fun getInstance(context: Context): MusicBuddyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicBuddyDatabase::class.java,
                    "playlist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}