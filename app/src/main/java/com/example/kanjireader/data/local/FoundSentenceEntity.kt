package com.example.kanjireader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "found_sentences",
    foreignKeys = [
        ForeignKey(
            entity = UserNoteEntity::class,
            parentColumns = arrayOf("character"),
            childColumns = arrayOf("characterOwner"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoundSentenceEntity(
    @PrimaryKey(autoGenerate = true) val sentenceId: Int = 0,
    val characterOwner: String,
    val sentence: String
)