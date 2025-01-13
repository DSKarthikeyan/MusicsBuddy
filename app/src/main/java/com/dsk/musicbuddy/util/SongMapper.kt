package com.dsk.musicbuddy.util

import com.dsk.musicbuddy.data.model.Song
import com.dsk.musicbuddy.data.model.SongDetails

object SongMapper {
    fun toSongDetails(song: Song): SongDetails {
        return SongDetails(
            songId = song.id,
            songName = song.title,
            songArtistName = song.artist,
            songDurationLong = song.duration,
            songPath = song.songUri,
            isSongFavourite = song.isFavorite
        )
    }

    fun toSong(songDetails: SongDetails): Song {
        return Song(
            id = songDetails.songId,
            title = songDetails.songName ?: "Unknown Title",
            artist = songDetails.songArtistName ?: "Unknown Artist",
            duration = songDetails.songDurationLong,
            songUri = songDetails.songPath ?: "",
            albumArtUri = songDetails.songAlbumPath,
            isFavorite = songDetails.isSongFavourite,
            isSongPlaying = false
        )
    }
}
