package com.dsk.musicbuddy.util

object StringConstants {
    const val sharedPreferenceName: String = "MusicBuddy"
    const val sharedPreferenceNameSongList: String = "music-buddy-list"
    const val sharedPreferenceNameQueue: String = "music-buddy-queue"
    const val sharedPreferenceSongManualyPaused: String = "is-song-manualy-paused"
    const val sharedPreferenceisUIUpdated: String = "is-ui-updated"
    const val sharedPreferenceisSongData: String = "song-data"
    const val sharedPreferenceSongScreenDetails: String = "song-playing-details"
    const val sharedPreferenceAppTheme: String = "app-theme"
    const val maxImageSize: Float = 1020f

    const val Channel_ID: String = "MusicBuddy"
    const val Channel_Name: String = "MusicBuddy"

    const val ACTION_STRING_ACTIVITY: String = "updateUI"

    const val TAG_FOREGROUND_SERVICE: String = "FOREGROUND_SERVICE"

    //    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    const val ACTION_START_FOREGROUND_SERVICE_BACKGROUND: String =
        "ACTION_START_FOREGROUND_SERVICE_BACKGROUND"
    const val ACTION_STOP_FOREGROUND_SERVICE: String = "ACTION_STOP_FOREGROUND_SERVICE"
    const val ACTION_PAUSE: String = "ACTION_PAUSE"
    const val ACTION_PREVIOUS: String = "ACTION_PREVIOUS"
    const val ACTION_NEXT: String = "ACTION_NEXT"
    const val ACTION_PLAY: String = "ACTION_PLAY"

    var FOREGROUND_SERVICE: Int = 101
}