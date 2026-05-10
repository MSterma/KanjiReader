package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.ui.components.*

@Composable
fun KanjiListDetailScreen(viewModel: KanjiViewModel, wejscieTekst: String) {
    LaunchedEffect(wejscieTekst) {
        viewModel.processText(wejscieTekst)
    }

    val charList by viewModel.charList.collectAsState()
    val selectedData by viewModel.selectedData.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            KanjiList(
                lista = charList,
                onClick = {
                    viewModel.getChar(it)
                    isEditing = false
                }
            )
        }

        Box(modifier = Modifier.weight(2f)) {
            if (isEditing && selectedData != null) {
                KanjiNoteEditor(
                    initialNote = selectedData?.userNotes?.userNote?.note ?: "",
                    onSave = { newNote ->
                        viewModel.updateNote(selectedData!!.dictionaryInfo!!.character.toString(), newNote)
                        isEditing = false
                    },
                    onCancel = { isEditing = false }
                )
            } else {
                KanjiDetail(
                    data = selectedData,
                    onEditClick = { isEditing = true }
                )
            }
        }
    }
}