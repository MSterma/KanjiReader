package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.ui.components.*
import androidx.activity.compose.BackHandler

@Composable
fun KanjiListDetailScreen(viewModel: KanjiViewModel, wejscieTekst: String) {
    LaunchedEffect(wejscieTekst) {
        viewModel.processText(wejscieTekst)
    }

    val charList by viewModel.charList.collectAsState()
    val selectedData by viewModel.selectedData.collectAsState()
    val currentSentence by viewModel.fullText.collectAsState()
    val tempEditText by viewModel.tempEditNoteText.collectAsState()
    val initializedChar by viewModel.initializedForCharacter.collectAsState()
    val selectedCharacter by viewModel.selectedCharacter.collectAsState()
    val dialogVisible by viewModel.dialogVisible.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    val currentCharacter = selectedData?.dictionaryInfo?.character?.toString() ?: ""

    BackHandler(enabled = dialogVisible) {
        if (isEditing) {
            viewModel.clearTempEditNoteText()
            viewModel.clearInitializedForCharacter()
            isEditing = false
        } else {
            viewModel.setDialogVisible(false)
            viewModel.setSelectedCharacter(null)
        }
    }

    LaunchedEffect(dialogVisible, isEditing, currentCharacter) {
        if (dialogVisible && isEditing && currentCharacter.isNotBlank() && initializedChar != currentCharacter) {
            val currentNote = selectedData?.userNotes?.userNote?.note ?: ""
            viewModel.updateTempEditNoteText(currentNote)
            viewModel.markInitializedForCharacter(currentCharacter)
        }
    }

    LaunchedEffect(selectedCharacter) {
        val char = selectedCharacter
        if (char != null && !dialogVisible) {
            viewModel.getChar(char)
            viewModel.setDialogVisible(true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        KanjiList(
            lista = charList,
            onClick = {
                viewModel.setSelectedCharacter(it)
            }
        )

        if (dialogVisible && selectedData != null) {
            Dialog(onDismissRequest = {
                if (isEditing) {
                    viewModel.clearTempEditNoteText()
                    viewModel.clearInitializedForCharacter()
                    isEditing = false
                } else {
                    viewModel.setDialogVisible(false)
                    viewModel.setSelectedCharacter(null)
                }
            }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditing) {
                        KanjiNoteEditor(
                            noteText = tempEditText,
                            currentSentence = currentSentence,
                            onTextChange = { viewModel.updateTempEditNoteText(it) },
                            onSave = { newNote, includeSentence ->
                                viewModel.saveNoteWithSentence(
                                    character = currentCharacter,
                                    note = newNote,
                                    includeSentence = includeSentence
                                )
                                isEditing = false
                                viewModel.setDialogVisible(false)
                                viewModel.setSelectedCharacter(null)
                                viewModel.clearTempEditNoteText()
                                viewModel.clearInitializedForCharacter()
                            },
                            onCancel = {
                                isEditing = false
                                viewModel.setDialogVisible(false)
                                viewModel.setSelectedCharacter(null)
                                viewModel.clearTempEditNoteText()
                                viewModel.clearInitializedForCharacter()
                            }
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