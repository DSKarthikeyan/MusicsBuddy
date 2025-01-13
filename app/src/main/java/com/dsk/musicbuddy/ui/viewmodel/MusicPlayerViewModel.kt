package com.dsk.musicbuddy.ui.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsk.musicbuddy.data.model.Song
import com.dsk.musicbuddy.data.repository.MusicRepository
import com.dsk.musicbuddy.util.MusicService
import com.dsk.musicbuddy.util.Utility

class MusicPlayerViewModel(var context: Context) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null

    private val repository: MusicRepository = MusicRepository(context)

    private val _songList = MutableLiveData<List<Song>>()
    val songList: LiveData<List<Song>> get() = _songList

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> get() = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private var musicService: MusicService? = null

    init {
        loadSongs()
    }

    private fun loadSongs() {
        if (repository.checkReadPermission()) {
            _songList.postValue(repository.getAllSongs())
        } else {
            // Handle permission denial scenario
        }
    }

    fun bindService(service: MusicService) {
        musicService = service
    }

    fun unbindService() {
        musicService = null
    }

    fun playSong(song: Song) {
        Log.d("DsK","Song Title play ${song.title}")
        _currentSong.postValue(song)
        song.isSongPlaying = true
        _isPlaying.postValue(true)
        musicService?.playSong(song.songUri)
    }

    fun pauseSong() {
        musicService?.pauseSong()
        _isPlaying.postValue(false)
        // Update the song's isSongPlaying property to false
        _currentSong.value?.let {
            it.isSongPlaying = false
            _currentSong.postValue(it)
        }
    }

    fun resumeSong() {
        Log.d("DsK","Song Title resumeSong")
        musicService?.resumeSong()
        _isPlaying.postValue(true)
    }

    fun seekTo(position: Int) {
        musicService?.mediaPlayer?.seekTo(position)
    }

    fun isPlaying(): Boolean {
        Log.d("DsK","Song Title isPlaying")
        return _isPlaying.value == true
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }

    fun deleteSong(song: Song) {
        // Remove the song from the repository if needed
        val success = repository.deleteSong(song)

        if (success) {
            Utility.deleteFile(context, song.songUri)
            // Update the observable song list
            _songList.postValue(_songList.value?.filterNot { it.id == song.id })

            // If the deleted song is the currently playing song, stop playback
            if (_currentSong.value?.id == song.id) {
                pauseSong()
                _currentSong.postValue(null)
            }
        } else {
            Log.e("MusicPlayerViewModel", "Failed to delete song: ${song.title}")
        }
    }

}