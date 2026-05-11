// data/Repository/KanjiRepository.kt
package com.example.kanjireader.data.Repository

import com.example.kanjireader.data.Model.KanjiInfo
import com.example.kanjireader.data.local.KanjiDao
import com.example.kanjireader.data.local.UserNoteDao
import com.example.kanjireader.data.local.UserNoteEntity
import com.example.kanjireader.data.local.FoundSentenceEntity
import com.example.kanjireader.data.local.NoteWithSentences
import com.example.kanjireader.data.remote.AuthManager
import kotlinx.coroutines.tasks.await

data class FullKanjiData(
    val dictionaryInfo: KanjiInfo?,
    val userNotes: NoteWithSentences?
)

class KanjiRepository (
    private val kanjiDao: KanjiDao,
    private val userNoteDao: UserNoteDao,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore,
    private val authManager: AuthManager
) {
    suspend fun getFullKanjiDetails(character: Char): FullKanjiData {
        val targetStr = character.toString()

        val entity = kanjiDao.geKanji(targetStr)
        val dictionaryInfo = if (entity != null) {
            KanjiInfo(
                character = entity.character.first(),
                meaning = entity.meaning,
                kunyomi = entity.kunyomi,
                onyomi = entity.onyomi
            )
        } else null

        val userNotes = userNoteDao.getNoteWithSentences(targetStr)

        return FullKanjiData(dictionaryInfo, userNotes)
    }

    suspend fun fetchKanjiData(character: Char): KanjiInfo? {
        val entity = kanjiDao.geKanji(character.toString()) ?: return null
        return KanjiInfo(
            character = entity.character.first(),
            meaning = entity.meaning,
            kunyomi = entity.kunyomi,
            onyomi = entity.onyomi
        )
    }



    suspend fun addSentence(character: String, sentence: String) {
        userNoteDao.insertInitialNote(UserNoteEntity(character, ""))
        userNoteDao.insertSentence(
            FoundSentenceEntity(characterOwner = character, sentence = sentence)
        )
    }

    fun getKanji(tekst: String): List<Char> {
        return tekst.filter { it in '\u4E00'..'\u9FAF' }.toSet().toList()
    }
    suspend fun logoutUser() {
        userNoteDao.clearAllSentences()
        userNoteDao.clearAllNotes()
        authManager.signOut()
    }
    suspend fun saveNote(character: String, note: String) {
        userNoteDao.insertInitialNote(UserNoteEntity(character, ""))
        userNoteDao.updateNote(character, note)
        authManager.getUserId()?.let { uid ->
            val remoteData = mapOf("note" to note)
            firestore.collection("users").document(uid)
                .collection("kanji_notes").document(character)
                .set(remoteData, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    fun syncFromCloud() {
        val uid = authManager.getUserId() ?: return
        firestore.collection("users").document(uid)
            .collection("kanji_notes").get()
            .addOnSuccessListener { docs ->
            }
    }
    suspend fun getAllUserNotes(): List<UserNoteEntity> = userNoteDao.getAllNotes()

    suspend fun searchUserNotes(query: String): List<UserNoteEntity> {
        if (query.isBlank()) {
            return userNoteDao.getAllNotes()
        }

        val matchingChars = kanjiDao.searchMatchingCharacters(query)

        return userNoteDao.searchNotesAdvanced(query, matchingChars)
    }    suspend fun syncNotes()  {
        val uid = authManager.getUserId() ?: return

        try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("kanji_notes").get().await()

            for (document in snapshot.documents) {
                val character = document.id
                val remoteNote = document.getString("note")

                if (remoteNote != null) {
                    userNoteDao.insertInitialNote(UserNoteEntity(character, ""))
                    userNoteDao.updateNote(character, remoteNote)
                }
            }

            val localNotes = userNoteDao.getAllNotes()
            for (local in localNotes) {
                if (local.note != null) {
                    val remoteData = mapOf("note" to local.note)
                    firestore.collection("users").document(uid)
                        .collection("kanji_notes").document(local.character)
                        .set(remoteData, com.google.firebase.firestore.SetOptions.merge())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}