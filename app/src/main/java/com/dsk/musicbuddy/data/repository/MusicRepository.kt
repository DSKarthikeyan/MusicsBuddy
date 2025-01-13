package com.dsk.musicbuddy.data.repository

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.dsk.musicbuddy.data.model.Song

class MusicRepository(private val context: Context) {

    fun getAllSongs(): List<Song> {
        val songsList = mutableListOf<Song>()
        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID // Add album ID to projection
        )

        if (checkReadPermission()) {
            val cursor = context.contentResolver.query(
                musicUri, projection, null, null, MediaStore.Audio.Media.TITLE + " ASC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val duration = it.getLong(durationColumn)
                    val albumId = it.getLong(albumIdColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )

                    // Fetch album art using album ID
                    val albumArtUri = getAlbumArtUri(albumId)

                    songsList.add(Song(
                        id, title, artist, duration, contentUri.toString(),
                        albumArtUri, // Pass album art URI
                        isFavorite = false,
                        isSongPlaying = false
                    ))
                }
            } ?: run {
                println("Error: Cursor is null.")
            }
        } else {
            println("Error: Permission not granted.")
        }

        return songsList
    }

    private fun getAlbumArtUri(albumId: Long): String? {
        val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        val selection = "${MediaStore.Audio.Albums._ID} = ?"
        val selectionArgs = arrayOf(albumId.toString())

        context.contentResolver.query(albumUri, projection, selection, selectionArgs, null)?.use {
            if (it.moveToFirst()) {
                val albumArtColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)
                return it.getString(albumArtColumn)
            }
        }
        return null
    }

    fun checkReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun deleteSong(song: Song): Boolean {
        try {
            val songUri = Uri.parse(song.songUri)
            val rowsDeleted = context.contentResolver.delete(songUri, null, null)
            return rowsDeleted > 0
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error deleting song: ${e.localizedMessage}")
            return false
        }
    }
}


