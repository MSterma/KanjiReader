package com.example.kanjireader.ViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjireader.data.Repository.FullKanjiData
import com.example.kanjireader.data.Repository.KanjiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KanjiViewModel(
    private val repository: KanjiRepository
) : ViewModel() {

    private val _charList = MutableStateFlow<List<Char>>(emptyList())
    val charList: StateFlow<List<Char>> = _charList.asStateFlow()

    private val _selectedData = MutableStateFlow<FullKanjiData?>(null)
    val selectedData: StateFlow<FullKanjiData?> = _selectedData.asStateFlow()

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
}