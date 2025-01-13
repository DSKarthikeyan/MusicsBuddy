package com.dsk.musicbuddy.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dsk.musicbuddy.data.model.Song

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Query("SELECT * FROM song_table")
    fun getAllSongs(): LiveData<List<Song>>

    @Query("SELECT * FROM song_table WHERE id = :songId")
    fun getSongById(songId: Long): LiveData<Song>

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)
}