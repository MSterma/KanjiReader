package com.example.kanjireader.ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjireader.data.Model.KanjiInfo
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

    private val _selectedChar = MutableStateFlow<KanjiInfo?>(null)
    val selectedChar: StateFlow<KanjiInfo?> = _selectedChar.asStateFlow()

    fun processText(text: String) {
        _charList.value = repository.getKanji(text)
    }

    fun getChar(character: Char) {
        viewModelScope.launch {
            _selectedChar.value = repository.fetchKanjiData(character)
        }    }
}