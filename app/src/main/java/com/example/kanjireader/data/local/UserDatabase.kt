package com.example.kanjireader.data.local


import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [UserNoteEntity::class, FoundSentenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userNoteDao(): UserNoteDao
}