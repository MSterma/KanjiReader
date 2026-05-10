package com.example.kanjireader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kanji")
data class KanjiEntity(
    @PrimaryKey val character: String,
    val meaning: String,
    val kunyomi: String,
    val onyomi: String
)