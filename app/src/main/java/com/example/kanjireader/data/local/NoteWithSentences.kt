package com.example.kanjireader.data.local


import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithSentences(
    @Embedded val userNote: UserNoteEntity,
    @Relation(
        parentColumn = "character",
        entityColumn = "characterOwner"
    )
    val sentences: List<FoundSentenceEntity>
)