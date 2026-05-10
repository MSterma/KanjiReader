// data/Repository/KanjiRepository.kt
package com.example.kanjireader.data.Repository

import com.example.kanjireader.data.Model.KanjiInfo
import com.example.kanjireader.data.local.KanjiDao
import com.example.kanjireader.data.local.UserNoteDao
// Assume these entities are in the same package as UserNoteDao
import com.example.kanjireader.data.local.UserNoteEntity
import com.example.kanjireader.data.local.FoundSentenceEntity
import com.example.kanjireader.data.local.NoteWithSentences

// Data class to hold both dictionary data and user data
data class FullKanjiData(
    val dictionaryInfo: KanjiInfo?,
    val userNotes: NoteWithSentences?
)

class KanjiRepository (
    private val kanjiDao: KanjiDao,
    private val userNoteDao: UserNoteDao
) {
    // New function fetching from both databases
    suspend fun getFullKanjiDetails(character: Char): FullKanjiData {
        val targetStr = character.toString()

        // 1. Fetch from dictionary
        val entity = kanjiDao.geKanji(targetStr)
        val dictionaryInfo = if (entity != null) {
            KanjiInfo(
                character = entity.character.first(),
                meaning = entity.meaning,
                kunyomi = entity.kunyomi,
                onyomi = entity.onyomi
            )
        } else null

        // 2. Fetch from user notes
        val userNotes = userNoteDao.getNoteWithSentences(targetStr)

        return FullKanjiData(dictionaryInfo, userNotes)
    }

    // Original function kept for compatibility
    suspend fun fetchKanjiData(character: Char): KanjiInfo? {
        val entity = kanjiDao.geKanji(character.toString()) ?: return null
        return KanjiInfo(
            character = entity.character.first(),
            meaning = entity.meaning,
            kunyomi = entity.kunyomi,
            onyomi = entity.onyomi
        )
    }

    // Save user note
    suspend fun saveNote(character: String, note: String) {
        userNoteDao.insertInitialNote(UserNoteEntity(character, ""))
        userNoteDao.updateNote(character, note)
    }

    // Add new sentence
    suspend fun addSentence(character: String, sentence: String) {
        userNoteDao.insertInitialNote(UserNoteEntity(character, ""))
        userNoteDao.insertSentence(
            FoundSentenceEntity(characterOwner = character, sentence = sentence)
        )
    }

    fun getKanji(tekst: String): List<Char> {
        return tekst.filter { it in '\u4E00'..'\u9FAF' }.toSet().toList()
    }
}