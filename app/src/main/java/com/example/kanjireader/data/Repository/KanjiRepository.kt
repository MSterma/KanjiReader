// data/Repository/KanjiRepository.kt
package com.example.kanjireader.data.Repository

import com.example.kanjireader.data.Model.KanjiInfo
import com.example.kanjireader.data.local.KanjiDao
import com.example.kanjireader.data.local.UserNoteDao
import com.example.kanjireader.data.local.UserNoteEntity
import com.example.kanjireader.data.local.FoundSentenceEntity
import com.example.kanjireader.data.local.NoteWithSentences
import com.example.kanjireader.data.remote.AuthManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
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
    }


    suspend fun updateNoteWithSentence(character: String, note: String, sentenceToAdd: String?) {
        userNoteDao.insertInitialNote(UserNoteEntity(character, ""))

        userNoteDao.updateNote(character, note)

        if (sentenceToAdd != null && sentenceToAdd.isNotBlank()) {
            val currentData = userNoteDao.getNoteWithSentences(character)
            val alreadyExists = currentData?.sentences?.any { it.sentence == sentenceToAdd } == true

            if (!alreadyExists) {
                userNoteDao.insertSentence(FoundSentenceEntity(characterOwner = character, sentence = sentenceToAdd))
            }
        }

        val uid = authManager.getUserId() ?: return
        val docRef = firestore.collection("users").document(uid)
            .collection("kanji_notes").document(character)

        val updates = mutableMapOf<String, Any>("note" to note)

        if (sentenceToAdd != null && sentenceToAdd.isNotBlank()) {
            updates["sentences"] = FieldValue.arrayUnion(sentenceToAdd)
        }

        docRef.set(updates, SetOptions.merge())
    }

    suspend fun syncNotes() {
        val uid = authManager.getUserId() ?: throw Exception("Brak autoryzacja")

        val snapshot = firestore.collection("users").document(uid)
            .collection("kanji_notes").get(com.google.firebase.firestore.Source.SERVER).await()

        val remoteMap = snapshot.documents.associateBy({ it.id }, { it.getString("note") ?: "" })
        val localList = userNoteDao.getAllNotes()
        val localMap = localList.associateBy({ it.character }, { it.note ?: "" })

        for (local in localList) {
            val remoteNote = remoteMap[local.character]
            val localNoteText = local.note ?: ""

            if (localNoteText.isNotEmpty() && localNoteText != remoteNote) {
                val data = mapOf("note" to localNoteText)
                firestore.collection("users").document(uid)
                    .collection("kanji_notes").document(local.character)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
        }

        for ((char, remoteNote) in remoteMap) {
            val localNote = localMap[char]
            if (localNote == null || (localNote.isEmpty() && remoteNote.isNotEmpty())) {
                userNoteDao.insertInitialNote(UserNoteEntity(char, ""))
                userNoteDao.updateNote(char, remoteNote)
            }
        }
    }
}