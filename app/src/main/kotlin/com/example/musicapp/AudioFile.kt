package com.example.musicapp

// Data class for AudioFile

data class AudioFile(
    val id: Long,
    val title: String,
    val artist: String?,
    val duration: Long,
    val filePath: String,
    val thumbnail: String?
)