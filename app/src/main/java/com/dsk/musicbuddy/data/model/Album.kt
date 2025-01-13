package com.dsk.musicbuddy.data.model

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val albumArtUri: String?,
)