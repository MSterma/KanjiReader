package com.example.kanjireader.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjireader.data.Model.PopupMessage
import com.example.kanjireader.data.Repository.FullKanjiData
import com.example.kanjireader.data.Repository.KanjiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoteUIModel(
    val character: String,
    val meaning: String,
    val note: String?
)

class KanjiViewModel(
    private val repository: KanjiRepository,
) : ViewModel() {

    private val _charList = MutableStateFlow<List<Char>>(emptyList())
    val charList: StateFlow<List<Char>> = _charList.asStateFlow()

    private val _fullText = MutableStateFlow("")
    val fullText: StateFlow<String> = _fullText.asStateFlow()

    private val _selectedData = MutableStateFlow<FullKanjiData?>(null)
    val selectedData: StateFlow<FullKanjiData?> = _selectedData.asStateFlow()

    private val _userNotes = MutableStateFlow<List<NoteUIModel>>(emptyList())
    val userNotes: StateFlow<List<NoteUIModel>> = _userNotes.asStateFlow()

    private val _popupMessage = MutableStateFlow<PopupMessage?>(null)
    val popupMessage: StateFlow<PopupMessage?> = _popupMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _tempEditNoteText = MutableStateFlow("")
    val tempEditNoteText: StateFlow<String> = _tempEditNoteText.asStateFlow()

    private val _initializedForCharacter = MutableStateFlow<String?>(null)
    val initializedForCharacter: StateFlow<String?> = _initializedForCharacter.asStateFlow()

    private val _selectedCharacter = MutableStateFlow<Char?>(null)
    val selectedCharacter: StateFlow<Char?> = _selectedCharacter.asStateFlow()

    private val _dialogVisible = MutableStateFlow(false)
    val dialogVisible: StateFlow<Boolean> = _dialogVisible.asStateFlow()

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
            _selectedData.value = null
            _selectedData.value = repository.getFullKanjiDetails(character)
        }
    }

    fun saveNoteWithSentence(character: String, note: String, includeSentence: Boolean) {
        viewModelScope.launch {
            val sentence = if (includeSentence) _fullText.value else null
            repository.updateNoteWithSentence(character, note, sentence)
            _selectedData.value = repository.getFullKanjiDetails(character.first())
            showMessage("Note saved successfully")
            loadAllNotes()
        }
    }

    fun deleteNote(character: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(character)
                _userNotes.value = _userNotes.value.filter { it.character != character }
            } catch (_: Exception) {
                showMessage("Error deleting note", true)
            }
        }
    }

    fun clearAllNotes() {
        viewModelScope.launch {
            try {
                repository.clearAllNotes()
                _userNotes.value = emptyList()
                _selectedData.value = null
                showMessage("All notes cleared")
            } catch (_: Exception) {
                showMessage("Error clearing notes", true)
            }
        }
    }

    fun clearLocalDataForAccountDeletion() {
        viewModelScope.launch {
            repository.clearAllNotes()
            _userNotes.value = emptyList()
            _selectedData.value = null
        }
    }

    fun syncData() {
        viewModelScope.launch {
            try {
                repository.syncNotes()
                loadAllNotes()
            } catch (_: Exception) {
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logoutUser()
            _selectedData.value = null
            _userNotes.value = emptyList()
            onLogoutComplete()
        }
    }

    fun loadAllNotes() {
        viewModelScope.launch {
            val notes = repository.getAllUserNotes()
            val uiModels = notes.map { note ->
                val kanjiData = repository.fetchKanjiData(note.character.first())
                NoteUIModel(
                    character = note.character,
                    meaning = kanjiData?.meaning ?: "Unknown",
                    note = note.note
                )
            }
            _userNotes.value = uiModels
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadAllNotes()
                return@launch
            }
            val notes = repository.getAllUserNotes()
            val uiModels = notes.map { note ->
                val kanjiData = repository.fetchKanjiData(note.character.first())
                NoteUIModel(
                    character = note.character,
                    meaning = kanjiData?.meaning ?: "Unknown",
                    note = note.note
                )
            }
            val filtered = uiModels.filter {
                it.character.contains(query, ignoreCase = true) ||
                        it.meaning.contains(query, ignoreCase = true) ||
                        (it.note?.contains(query, ignoreCase = true) == true)
            }
            _userNotes.value = filtered
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchNotes(query)
    }

    fun updateTempEditNoteText(text: String) {
        _tempEditNoteText.value = text
    }

    fun clearTempEditNoteText() {
        _tempEditNoteText.value = ""
    }

    fun markInitializedForCharacter(character: String) {
        _initializedForCharacter.value = character
    }

    fun clearInitializedForCharacter() {
        _initializedForCharacter.value = null
    }

    fun setSelectedCharacter(character: Char?) {
        _selectedCharacter.value = character
    }

    fun setDialogVisible(visible: Boolean) {
        _dialogVisible.value = visible
    }
}