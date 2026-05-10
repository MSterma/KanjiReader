package com.example.kanjireader.data.local



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserNoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialNote(note: UserNoteEntity)

    @Query("UPDATE user_notes SET note = :newNote WHERE character = :targetCharacter")
    suspend fun updateNote(targetCharacter: String, newNote: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentence(sentence: FoundSentenceEntity)


    @Transaction
    @Query("SELECT * FROM user_notes WHERE character = :targetCharacter LIMIT 1")
    suspend fun getNoteWithSentences(targetCharacter: String): NoteWithSentences?
}