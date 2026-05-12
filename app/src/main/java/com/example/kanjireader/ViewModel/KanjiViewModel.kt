package com.example.kanjireader.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjireader.data.Model.PopupMessage
import com.example.kanjireader.data.Repository.FullKanjiData
import com.example.kanjireader.data.Repository.KanjiRepository
import com.example.kanjireader.data.local.UserNoteEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KanjiViewModel(
    private val repository: KanjiRepository,
) : ViewModel() {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _charList = MutableStateFlow<List<Char>>(emptyList())
    val charList: StateFlow<List<Char>> = _charList.asStateFlow()

    private val _fullText = MutableStateFlow("")
    val fullText: StateFlow<String> = _fullText.asStateFlow()

    private val _selectedData = MutableStateFlow<FullKanjiData?>(null)
    val selectedData: StateFlow<FullKanjiData?> = _selectedData.asStateFlow()

    private val _userNotes = MutableStateFlow<List<UserNoteEntity>>(emptyList())
    val userNotes: StateFlow<List<UserNoteEntity>> = _userNotes.asStateFlow()

    private val _popupMessage = MutableStateFlow<PopupMessage?>(null)
    val popupMessage: StateFlow<PopupMessage?> = _popupMessage.asStateFlow()

    fun showMessage(text: String, isError: Boolean = false) {
        viewModelScope.launch {
            _popupMessage.value = PopupMessage(text, isError)
            delay(3000)
            _popupMessage.value = null
        }
    }

    fun processText(text: String) {
        _fullText.value = text
        val kanji = repository.getKanji(text)
        _charList.value = kanji

        if (kanji.isEmpty() && text.isNotBlank()) {
            showMessage("Sorry, unable to retrieve any character", true)
        }
    }

    fun getChar(character: Char) {
        viewModelScope.launch {
            _selectedData.value = repository.getFullKanjiDetails(character)
        }
    }

    fun updateNote(character: String, note: String) {
        viewModelScope.launch {
            repository.saveNote(character, note)
            _selectedData.value = repository.getFullKanjiDetails(character.first())
        }
    }

    fun saveNoteWithSentence(character: String, note: String, includeSentence: Boolean) {
        viewModelScope.launch {
            val sentence = if (includeSentence) _fullText.value else null
            repository.updateNoteWithSentence(character, note, sentence)
            _selectedData.value = repository.getFullKanjiDetails(character.first())
        }
    }

    fun syncData() {
        viewModelScope.launch {
            try {
                repository.syncNotes()
            } catch (e: Exception) {
                showMessage("Couldn't synchronize with server", true)
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logoutUser()
            _selectedData.value = null
            onLogoutComplete()
        }
    }

    fun loadAllNotes() {
        viewModelScope.launch {
            _userNotes.value = repository.getAllUserNotes()
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            _userNotes.value = repository.searchUserNotes(query)
        }
    }
}