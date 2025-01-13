package com.dsk.musicbuddy.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.dsk.musicbuddy.data.model.Song
import com.dsk.musicbuddy.data.repository.MusicRepository

class MusicPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var musicRepository: MusicRepository
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongIndex = 0
    private var songList: List<Song> = emptyList()

    override fun onCreate() {
        super.onCreate()

        // Initialize MusicRepository
        musicRepository = MusicRepository(this)

        // Load songs from the repository
        songList = musicRepository.getAllSongs()

        mediaSession = MediaSessionCompat(this, "MusicPlaybackService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    if (songList.isNotEmpty()) {
                        playSong(currentSongIndex)
                    }
                }

                override fun onPause() {
                    pausePlayback()
                }

                override fun onSkipToNext() {
                    if (songList.isNotEmpty()) {
                        currentSongIndex = (currentSongIndex + 1) % songList.size
                        playSong(currentSongIndex)
                    }
                }

                override fun onSkipToPrevious() {
                    if (songList.isNotEmpty()) {
                        currentSongIndex = if (currentSongIndex - 1 < 0) {
                            songList.size - 1
                        } else {
                            currentSongIndex - 1
                        }
                        playSong(currentSongIndex)
                    }
                }

                override fun onStop() {
                    stopPlayback()
                    stopForeground(true)
                    stopSelf()
                }
            })

            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_STOP
                    )
                    .build()
            )

            isActive = true
        }

        sessionToken = mediaSession.sessionToken
    }

    private fun playSong(index: Int) {
        val song = songList[index]

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
                .build()
        )

        mediaPlayer?.release() // Release any previous player
        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, Uri.parse(song.songUri))
            setOnPreparedListener {
                it.start()
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                        .build()
                )
            }
            setOnCompletionListener {
                mediaSession.controller.transportControls.skipToNext()
            }
            prepareAsync()
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1f)
                        .build()
                )
            }
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
                .build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                StringConstants.ACTION_PLAY -> mediaSession.controller.transportControls.play()
                StringConstants.ACTION_PAUSE -> mediaSession.controller.transportControls.pause()
                StringConstants.ACTION_NEXT -> mediaSession.controller.transportControls.skipToNext()
                StringConstants.ACTION_PREVIOUS -> mediaSession.controller.transportControls.skipToPrevious()
                StringConstants.ACTION_STOP_FOREGROUND_SERVICE -> mediaSession.controller.transportControls.stop()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        val mediaItems = songList.map { song ->
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(song.id.toString())
                    .setTitle(song.title)
                    .setSubtitle(song.artist)
                    .setIconUri(song.albumArtUri?.let { Uri.parse(it) })
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        }
        result.sendResult(mediaItems)
    }

    override fun onDestroy() {
        stopPlayback()
        mediaSession.release()
        super.onDestroy()
    }
}



