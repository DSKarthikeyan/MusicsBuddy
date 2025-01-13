package com.dsk.musicbuddy.data.model

import android.graphics.Bitmap
import java.io.Serializable

data class SongDetails(
    var songId: Long = 0,
    var albumId: Long = 0,
    var artistId: Long = 0,
    var genresId: Long = 0,
    var songPosition: Int = 0,
    var songName: String? = null,
    var songAlbumName: String? = null,
    var songArtistName: String? = null,
    var songDurationString: String? = null,
    var songAlbumImage: Bitmap? = null,
    var songDurationLong: Long = 0,
    var songPath: String? = null,
    var songAlbumPath: String? = null,
    var isSongFavourite: Boolean = false,
    var isSongPlaying: Boolean = false,
    var isChecked: Boolean = false
) : Serializable