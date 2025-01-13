package com.dsk.musicbuddy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_table")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val songUri: String,
    val albumArtUri: String?,
    var isFavorite: Boolean = false,
    var isSongPlaying: Boolean
)