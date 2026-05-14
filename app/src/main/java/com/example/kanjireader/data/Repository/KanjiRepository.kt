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
                .set(remoteData, SetOptions.merge())
        }
    }

    suspend fun getAllUserNotes(): List<UserNoteEntity> = userNoteDao.getAllNotes()

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

    suspend fun deleteNote(character: String) {
        userNoteDao.deleteSentencesForCharacter(character)
        userNoteDao.deleteNote(character)

        authManager.getUserId()?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("kanji_notes").document(character)
                .delete()
        }
    }

    suspend fun clearAllNotes() {
        userNoteDao.clearAllSentences()
        userNoteDao.clearAllNotes()

        val uid = authManager.getUserId() ?: return
        try {
            val docs = firestore.collection("users").document(uid)
                .collection("kanji_notes").get().await()
            for (doc in docs.documents) {
                doc.reference.delete()
            }
        } catch (_: Exception) {}
    }

    suspend fun syncNotes() {
        val uid = authManager.getUserId() ?: throw Exception("Unauthorized")

        val snapshot = firestore.collection("users").document(uid)
            .collection("kanji_notes").get(Source.SERVER).await()

        val remoteDocs = snapshot.documents

        for (doc in remoteDocs) {
            val char = doc.id
            val remoteNote = doc.getString("note") ?: ""
            val remoteSentences = (doc.get("sentences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            userNoteDao.insertInitialNote(UserNoteEntity(char, ""))

            val localData = userNoteDao.getNoteWithSentences(char)
            val localNote = localData?.userNote?.note ?: ""
            val localSentences = localData?.sentences?.map { it.sentence } ?: emptyList()

            if (localNote.isEmpty() && remoteNote.isNotEmpty()) {
                userNoteDao.updateNote(char, remoteNote)
            }

            for (sentence in remoteSentences) {
                if (!localSentences.contains(sentence)) {
                    userNoteDao.insertSentence(FoundSentenceEntity(characterOwner = char, sentence = sentence))
                }
            }
        }

        val localList = userNoteDao.getAllNotes()
        for (local in localList) {
            val char = local.character
            val localNote = local.note ?: ""
            val localData = userNoteDao.getNoteWithSentences(char)
            val localSentences = localData?.sentences?.map { it.sentence } ?: emptyList()

            val doc = remoteDocs.find { it.id == char }
            val remoteNote = doc?.getString("note") ?: ""
            val remoteSentences = (doc?.get("sentences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val updates = mutableMapOf<String, Any>()

            if (localNote.isNotEmpty() && localNote != remoteNote) {
                updates["note"] = localNote
            }

            val hasMissingSentences = localSentences.any { !remoteSentences.contains(it) }
            if (hasMissingSentences) {
                val mergedSentences = (localSentences + remoteSentences).distinct()
                updates["sentences"] = mergedSentences
            }

            if (updates.isNotEmpty()) {
                firestore.collection("users").document(uid)
                    .collection("kanji_notes").document(char)
                    .set(updates, SetOptions.merge())
                    .await()
            }
        }
    }
}