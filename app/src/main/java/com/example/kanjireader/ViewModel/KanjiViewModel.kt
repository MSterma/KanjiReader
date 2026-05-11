package com.example.kanjireader.ViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjireader.data.Repository.FullKanjiData
import com.example.kanjireader.data.Repository.KanjiRepository
import com.example.kanjireader.data.local.UserNoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KanjiViewModel(
    private val repository: KanjiRepository,
) : ViewModel() {

    private val _charList = MutableStateFlow<List<Char>>(emptyList())
    val charList: StateFlow<List<Char>> = _charList.asStateFlow()

    private val _selectedData = MutableStateFlow<FullKanjiData?>(null)
    val selectedData: StateFlow<FullKanjiData?> = _selectedData.asStateFlow()
    private val _userNotes = MutableStateFlow<List<UserNoteEntity>>(emptyList())
    val userNotes: StateFlow<List<UserNoteEntity>> = _userNotes.asStateFlow()
    fun processText(text: String) {
        _charList.value = repository.getKanji(text)
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
    fun syncData() {
        viewModelScope.launch {
            repository.syncNotes()
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