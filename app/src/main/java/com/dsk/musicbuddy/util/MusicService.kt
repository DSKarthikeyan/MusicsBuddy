package com.dsk.musicbuddy.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.dsk.musicbuddy.data.model.Song
import java.io.IOException

class MusicService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    var mediaPlayer: MediaPlayer? = null
    private val binder = LocalBinder()
    private var currentSongUri: String? = null

    companion object {
        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize MediaSession
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    mediaPlayer?.start()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    startOrUpdateForeground(true)
                }

                override fun onPause() {
                    mediaPlayer?.pause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                    startOrUpdateForeground(false)
                    stopForeground(false)
                }

                override fun onStop() {
                    stopSong()
                }
            })
            isActive = true
        }

        // Restore saved state
        val preferences = getSharedPreferences("MusicServicePrefs", Context.MODE_PRIVATE)
        val savedSongUri = preferences.getString("currentSongUri", null)
        val savedPosition = preferences.getInt("currentPosition", 0)
        val savedPlaybackState = preferences.getInt("playbackState", PlaybackStateCompat.STATE_STOPPED)

        if (savedSongUri != null) {
            // Prepare the media player with the saved song URI and position
            playSong(savedSongUri)
            mediaPlayer?.seekTo(savedPosition)

            // Restore playback state but do not auto-play
            if (savedPlaybackState == PlaybackStateCompat.STATE_PLAYING) {
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                stopForeground(false)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                StringConstants.ACTION_PLAY -> mediaSession.controller.transportControls.play()
                StringConstants.ACTION_PAUSE -> mediaSession.controller.transportControls.pause()
                StringConstants.ACTION_NEXT -> mediaSession.controller.transportControls.skipToNext()
                StringConstants.ACTION_PREVIOUS -> mediaSession.controller.transportControls.skipToPrevious()
                StringConstants.ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopSong()
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startOrUpdateForeground(isSongPlaying: Boolean) {
        if (!::mediaSession.isInitialized) return
        val notification = CreateNotification.createNotification(
            this, mediaSession.controller.metadata, isSongPlaying
        )

        if (!isForegroundService()) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            getNotificationManager().notify(NOTIFICATION_ID, notification)
        }
    }

    private fun isForegroundService(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    private fun getNotificationManager(): NotificationManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(NotificationManager::class.java)
        } else {
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
    }

    private fun updatePlaybackState(state: Int) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, mediaPlayer?.currentPosition?.toLong() ?: 0L, 1f)
                .build()
        )
    }

    fun playSong(songUri: String) {
        try {
            if (currentSongUri == songUri && mediaPlayer?.isPlaying == true) {
                return
            }

            mediaPlayer?.stop()
            mediaPlayer?.release()

            Log.d("DsK","playSong $songUri")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MusicService, Uri.parse(songUri))
                prepare()
                start()
            }

            currentSongUri = songUri

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, songUri)
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    "Song Title"
                ) // Replace dynamically
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    "Artist Name"
                ) // Replace dynamically
                .build()

            mediaSession.setMetadata(metadata)
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            startOrUpdateForeground(true)
        } catch (e: IOException) {
            Log.e("MusicService", "Failed to set data source: ${e.localizedMessage}")
        }
    }

    fun pauseSong() {
        mediaPlayer?.pause()
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        startOrUpdateForeground(false)
        stopForeground(false)
    }

    fun resumeSong() {
        mediaPlayer?.start()
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        startOrUpdateForeground(true)
    }

    fun stopSong() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        stopForeground(true)
        startOrUpdateForeground(false)
    }

    private val songQueue: MutableList<Song> = mutableListOf()

    fun getSongQueue(): List<Song> = songQueue

    fun addToQueue(song: Song) {
        // Add the song to the end of the queue
        mediaPlayer?.let {
            songQueue.add(song)
        }
    }

    fun queueNext(song: Song) {
        // Insert the song at the top of the queue
        mediaPlayer?.let {
            songQueue.add(0, song)
        }
    }

    private fun savePlaybackState() {
        val preferences = getSharedPreferences("MusicServicePrefs", Context.MODE_PRIVATE)
        preferences.edit().apply {
            putString("currentSongUri", currentSongUri)
            putInt("currentPosition", mediaPlayer?.currentPosition ?: 0)
            putInt("playbackState", mediaSession.controller.playbackState?.state ?: PlaybackStateCompat.STATE_STOPPED)
            apply()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        savePlaybackState()
        val restartIntent = Intent(applicationContext, MusicService::class.java).also {
            it.setPackage(packageName)
        }
        val restartPendingIntent = PendingIntent.getService(
            applicationContext, 1, restartIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartPendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        savePlaybackState()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.release()
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder
}


//class MusicService : Service() {
//
//
//    private lateinit var mediaSession: MediaSessionCompat
//    var mediaPlayer: MediaPlayer? = null
//    private val binder = LocalBinder()
//    private var currentSongUri: String? = null
//
//    companion object {
//        const val CHANNEL_ID = "music_channel"
//        const val NOTIFICATION_ID = 101
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Initialize MediaSession
//        mediaSession = MediaSessionCompat(this, "MusicService").apply {
//            setCallback(object : MediaSessionCompat.Callback() {
//                override fun onPlay() {
//                    super.onPlay()
//                    mediaPlayer?.start()
//                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
//                    startOrUpdateForeground(true)
//                }
//
//                override fun onPause() {
//                    super.onPause()
//                    mediaPlayer?.pause()
//                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
//                    stopForeground(false)
//                }
//
//                override fun onStop() {
//                    super.onStop()
//                    Log.d("DsK","Service Session OnStop")
//                    stopSong()
//                }
//            })
//            isActive = true
//        }
//
//        val preferences = getSharedPreferences("MusicServicePrefs", Context.MODE_PRIVATE)
//        val savedSongUri = preferences.getString("currentSongUri", null)
//        val savedPosition = preferences.getInt("currentPosition", 0)
//
//        if (savedSongUri != null) {
//            playSong(savedSongUri, this)
//            mediaPlayer?.seekTo(savedPosition)
//        }
//
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
////        MediaButtonReceiver.handleIntent(mediaSession, intent)
//
//        if (intent != null && intent.action != null) {
//            if (intent.action == StringConstants.ACTION_PLAY && intent.extras != null) {
//                val songId = intent.extras!!.getLong("SongId")
////                playSong(songId)
//                Log.d("DsK","ACTION_PLAY $songId")
//                startOrUpdateForeground(true)
//            } else {
//                setMusicPlayerIntentAction(intent.action)
//            }
//        }
//        return START_NOT_STICKY
//    }
//
//    private fun setMusicPlayerIntentAction(getIntentAction: String?) {
////        if (!getIntentAction.isNullOrEmpty()) {
//////            if (getIntentAction.equals(StringConstants.ACTION_START_FOREGROUND_SERVICE)) {
//////                showNotification();
//////            } else
////            if (getIntentAction == StringConstants.ACTION_PREVIOUS) {
//////                playPrev()
////                Log.d("DsK","ACTION_PREVIOUS")
////            } else if (getIntentAction == StringConstants.ACTION_PAUSE) {
////                pauseSong()
////                Log.d("DsK","ACTION_PAUSE")
////            } else if (getIntentAction == StringConstants.ACTION_NEXT) {
//////                playNext()
////                Log.d("DsK","ACTION_NEXT")
////            } else if (getIntentAction == StringConstants.ACTION_START_FOREGROUND_SERVICE_BACKGROUND) {
////            } else if (getIntentAction == StringConstants.ACTION_STOP_FOREGROUND_SERVICE) {
////                Log.d("DsK","ACTION_STOP_FOREGROUND_SERVICE")
////                sendBroadcast()
////                stopForeground(true)
////                stopSelf()
////                mediaPlayer!!.stop()
////                stopService(Intent(this, MusicService::class.java))
////                //   removeAudioFocus();
////            }
////        }
//
//        when (getIntentAction) {
//            StringConstants.ACTION_PLAY -> {
//                Log.d("DsK","ACTION_PLAY")
//                mediaSession.controller.transportControls.play()
//            }
//            StringConstants.ACTION_PAUSE -> {
//                Log.d("DsK","ACTION_PAUSE")
//                mediaSession.controller.transportControls.pause()
//            }
//            StringConstants.ACTION_NEXT -> {
//                Log.d("DsK","ACTION_NEXT")
//                mediaSession.controller.transportControls.skipToNext()
//            }
//            StringConstants.ACTION_PREVIOUS -> {
//                Log.d("DsK","ACTION_PREVIOUS")
//                mediaSession.controller.transportControls.skipToPrevious()
//            }
//            StringConstants.ACTION_STOP_FOREGROUND_SERVICE -> {
//                Log.d("DsK","ACTION_STOP_FOREGROUND_SERVICE")
//                sendBroadcast()
//                stopForeground(true)
//                stopSelf()
//                stopService(Intent(this, MusicService::class.java))
//            }
//        }
//    }
//
//    private fun sendBroadcast() {
//        val newIntent = Intent()
//        newIntent.setAction(StringConstants.ACTION_STRING_ACTIVITY)
//        sendBroadcast(newIntent)
//    }
//
//    private fun startOrUpdateForeground(isSongPlaying: Boolean) {
//        if (!::mediaSession.isInitialized) return
//
//        val notification = CreateNotification.createNotification(this, mediaSession.controller.metadata, isSongPlaying, 0)
//         if (!isForegroundService()) {
//            startForeground(NOTIFICATION_ID, notification)
//        } else {
//            val notificationManager = getNotificationManager()
//            notificationManager.notify(NOTIFICATION_ID, notification)
//        }
//    }
//
//    private fun getNotificationManager(): NotificationManager {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getSystemService(NotificationManager::class.java)
//        } else {
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        }
//    }
//
//    private fun isForegroundService(): Boolean {
//        return mediaPlayer?.isPlaying == true
//    }
//
//    private fun updatePlaybackState(state: Int) {
//        mediaSession.setPlaybackState(
//            PlaybackStateCompat.Builder()
//                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
//                .build()
//        )
//    }
//
//    fun playSong(songUri: String, context: Context) {
//        try {
//            if (currentSongUri == songUri && mediaPlayer?.isPlaying == true) {
//                return
//            }
//
//            mediaPlayer?.stop()
//            mediaPlayer?.reset()
//
//            mediaPlayer = MediaPlayer().apply {
//                setDataSource(context, Uri.parse(songUri))
//                prepare()
//                start()
//            }
//
//            currentSongUri = songUri
//
//            val metadata = MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Song Title")
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artist Name")
//                .build()
//
//            mediaSession.setMetadata(metadata)
//
//            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
//            startOrUpdateForeground(true)
//        } catch (e: IOException) {
//            Log.e("MusicService", "Failed to set data source: ${e.localizedMessage}")
//        }
//    }
//
//    fun pauseSong() {
//        mediaPlayer?.pause()
//        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
//        stopForeground(false)
//    }
//
//    fun resumeSong() {
//        mediaPlayer?.start()
//        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
//        startOrUpdateForeground(true)
//    }
//
//    fun stopSong() {
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//        mediaPlayer = null
//        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
//        stopForeground(true)
//        stopSelf()
//    }
//
//    inner class LocalBinder : Binder() {
//        fun getService(): MusicService = this@MusicService
//    }
//
//    override fun onBind(intent: Intent?): IBinder = binder
//
//    override fun onDestroy() {
//        super.onDestroy()
//        savePlaybackState()
//        mediaPlayer?.release()
//        mediaPlayer = null
//        mediaSession.release()
//    }
//
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        savePlaybackState()
//
//        val restartServiceIntent = Intent(applicationContext, MusicService::class.java).also {
//            it.setPackage(packageName)
//        }
//        val restartServicePendingIntent = PendingIntent.getService(
//            applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
//        )
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmManager.set(
//            AlarmManager.ELAPSED_REALTIME,
//            SystemClock.elapsedRealtime() + 1000,
//            restartServicePendingIntent
//        )
//        super.onTaskRemoved(rootIntent)
//    }
//
//    private fun savePlaybackState() {
//        val preferences = getSharedPreferences("MusicServicePrefs", Context.MODE_PRIVATE)
//        preferences.edit().apply {
//            putString("currentSongUri", currentSongUri)
//            putInt("currentPosition", mediaPlayer?.currentPosition ?: 0)
//            apply()
//        }
//    }
//}


