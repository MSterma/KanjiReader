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
    @Query("SELECT * FROM user_notes")
    suspend fun getAllNotes(): List<UserNoteEntity>
    @Query("UPDATE user_notes SET note = :newNote WHERE character = :targetCharacter")
    suspend fun updateNote(targetCharacter: String, newNote: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentence(sentence: FoundSentenceEntity)

    @Query("DELETE FROM found_sentences")
    suspend fun clearAllSentences()

    @Query("DELETE FROM user_notes")
    suspend fun clearAllNotes()
    @Transaction
    @Query("SELECT * FROM user_notes WHERE character = :targetCharacter LIMIT 1")
    suspend fun getNoteWithSentences(targetCharacter: String): NoteWithSentences?

    @Query("SELECT * FROM user_notes WHERE note LIKE '%' || :query || '%'")
    suspend fun searchNotes(query: String): List<UserNoteEntity>

    @Query("""
    SELECT * FROM user_notes 
    WHERE note LIKE '%' || :query || '%' 
    OR character LIKE '%' || :query || '%' 
    OR character IN (:matchingChars)
""")
    suspend fun searchNotesAdvanced(query: String, matchingChars: List<String>): List<UserNoteEntity>
}