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
import com.dsk.musicbuddy.data.model.Album
import com.dsk.musicbuddy.data.model.Song
import java.io.File

class MusicRepository(private val context: Context) {

    fun getAllSongs(): List<Song> {
        val songsList = mutableListOf<Song>()
        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
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
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val duration = it.getLong(durationColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val album = it.getString(albumColumn) ?: "Unknown Album"

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )

                    // Fetch album art using album ID
                    val albumArtUri = getAlbumArtUri(albumId)

                    songsList.add(Song(
                        id, title, artist, duration, contentUri.toString(),
                        albumArtUri,
                        isFavorite = false,
                        isSongPlaying = false
                    ))
                }
            }
        }
        return songsList
    }

    fun getAlbumArtUri(albumId: Long): String? {
        val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART)
        val selection = "${MediaStore.Audio.Albums._ID} = ?"
        val selectionArgs = arrayOf(albumId.toString())

        context.contentResolver.query(albumUri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val albumArtColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)
                val albumArtPath = cursor.getString(albumArtColumn)

                // If the album art exists, return the content URI for it
                if (!albumArtPath.isNullOrEmpty()) {
                    val albumArtFile = File(albumArtPath)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        albumArtFile.name.toLong() // Retrieve the actual ID or use the correct method here
                    )
                    return contentUri.toString()  // Return content URI
                } else {
                    Log.e("MusicRepository", "No albumArtPath for Album ID: $albumId")
                }
            } else {
                Log.e("MusicRepository", "No album found for Album ID: $albumId")
            }
        } ?: run {
            Log.e("MusicRepository", "Failed to query MediaStore or null cursor returned.")
        }

        return null
    }

    fun getSongsByAlbum(albumId: Long): List<Song> {
        val songsList = mutableListOf<Song>()
        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
        )

        val selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?"
        val selectionArgs = arrayOf(albumId.toString())

        if (checkReadPermission()) {
            val cursor = context.contentResolver.query(
                musicUri, projection, selection, selectionArgs, MediaStore.Audio.Media.TITLE + " ASC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val duration = it.getLong(durationColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val album = it.getString(albumColumn) ?: "Unknown Album"

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )

                    val albumArtUri = getAlbumArtUri(albumId)

                    songsList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            duration =duration,
                            songUri = contentUri.toString(),
                            albumArtUri = albumArtUri,
                            isFavorite = false,
                            isSongPlaying = false
                        )
                    )
                }
            }
        }
        return songsList
    }

    fun getAllAlbums(): List<Album> {
        val albumList = mutableListOf<Album>()
        val albumUri: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ALBUM_ART
        )

        if (checkReadPermission()) {
            val cursor = context.contentResolver.query(
                albumUri, projection, null, null, MediaStore.Audio.Albums.ALBUM + " ASC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                val albumArtColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val albumName = it.getString(albumColumn) ?: "Unknown Album"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val albumArt = it.getString(albumArtColumn)

                    albumList.add(Album(id, albumName, artist, albumArt))
                }
            }
        }
        return albumList
    }

    fun getSongsByArtist(artistName: String): List<Song> {
        val songsList = mutableListOf<Song>()
        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.ARTIST} = ?"
        val selectionArgs = arrayOf(artistName)

        if (checkReadPermission()) {
            val cursor = context.contentResolver.query(
                musicUri, projection, selection, selectionArgs, MediaStore.Audio.Media.TITLE + " ASC"
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

                    val albumArtUri = getAlbumArtUri(albumId)

                    songsList.add(Song(
                        id, title, artist, duration, contentUri.toString(),
                        albumArtUri,isFavorite = false, isSongPlaying = false
                    ))
                }
            }
        }
        return songsList
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



