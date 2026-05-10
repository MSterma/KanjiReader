package com.example.kanjireader.data.remote

data class RemoteNote(
    val note: String = "",
    val sentences: List<String> = emptyList()
)