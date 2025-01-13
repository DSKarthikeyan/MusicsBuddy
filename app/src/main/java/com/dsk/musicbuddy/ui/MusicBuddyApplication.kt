package com.dsk.musicbuddy.ui

import android.app.Application
import androidx.room.Room
import com.dsk.musicbuddy.data.local.MusicBuddyDatabase
import com.dsk.musicbuddy.data.repository.MusicRepository

class MusicBuddyApplication : Application() {

    val songRepository: MusicRepository by lazy {
        MusicRepository(applicationContext)
    }

    private val database by lazy { MusicBuddyDatabase.getInstance(this) }
    val songDao by lazy { database.songDao() }
//    val playlistDao by lazy { database.playlistDao() }
//    val playlistSongDao by lazy { database.playlistSongDao() }
}