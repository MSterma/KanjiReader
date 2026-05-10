package com.example.kanjireader.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_notes")
data class UserNoteEntity(
    @PrimaryKey val character: String,
    val note: String
)