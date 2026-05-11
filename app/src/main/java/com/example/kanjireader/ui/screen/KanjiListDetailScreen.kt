package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.ui.components.*

@Composable
fun KanjiListDetailScreen(viewModel: KanjiViewModel, wejscieTekst: String) {
    LaunchedEffect(wejscieTekst) {
        viewModel.processText(wejscieTekst)
    }

    val charList by viewModel.charList.collectAsState()
    val selectedData by viewModel.selectedData.collectAsState()
    val currentSentence by viewModel.fullText.collectAsState()

    var isEditing by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        KanjiList(
            lista = charList,
            onClick = {
                viewModel.getChar(it)
                isEditing = false
                showDialog = true
            }
        )

        if (showDialog && selectedData != null) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditing) {
                        KanjiNoteEditor(
                            initialNote = selectedData?.userNotes?.userNote?.note ?: "",
                            currentSentence = currentSentence,
                            onSave = { newNote, includeSentence ->
                                viewModel.saveNoteWithSentence(
                                    character = selectedData!!.dictionaryInfo!!.character.toString(),
                                    note = newNote,
                                    includeSentence = includeSentence
                                )
                                isEditing = false
                                showDialog = false
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
    }
}