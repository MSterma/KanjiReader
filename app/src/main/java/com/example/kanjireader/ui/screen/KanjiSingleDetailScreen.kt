package com.example.kanjireader.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.ui.components.KanjiDetail
import com.example.kanjireader.ui.components.KanjiNoteEditor

@Composable
fun KanjiSingleDetailScreen(viewModel: KanjiViewModel, onBackClick: () -> Unit) {
    val selectedData by viewModel.selectedData.collectAsState()
    var isEditing by rememberSaveable { mutableStateOf(false) }
    val tempEditText by viewModel.tempEditNoteText.collectAsState()
    val initializedChar by viewModel.initializedForCharacter.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentCharacter = selectedData?.dictionaryInfo?.character?.toString() ?: ""

    LaunchedEffect(isEditing, currentCharacter) {
        if (isEditing && currentCharacter.isNotBlank() && initializedChar != currentCharacter) {
            val currentNote = selectedData?.userNotes?.userNote?.note ?: ""
            viewModel.updateTempEditNoteText(currentNote)
            viewModel.markInitializedForCharacter(currentCharacter)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    KanjiDetail(
                        data = selectedData,
                        onEditClick = { isEditing = true }
                    )
                }
                if (isEditing && selectedData != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        KanjiNoteEditor(
                            noteText = tempEditText,
                            currentSentence = "",
                            onTextChange = { viewModel.updateTempEditNoteText(it) },
                            onSave = { newNote, _ ->
                                viewModel.saveNoteWithSentence(
                                    character = currentCharacter,
                                    note = newNote,
                                    includeSentence = false
                                )
                                isEditing = false
                                viewModel.clearTempEditNoteText()
                                viewModel.clearInitializedForCharacter()
                            },
                            onCancel = {
                                isEditing = false
                                viewModel.clearTempEditNoteText()
                                viewModel.clearInitializedForCharacter()
                            }
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                if (isEditing && selectedData != null) {
                    KanjiNoteEditor(
                        noteText = tempEditText,
                        currentSentence = "",
                        onTextChange = { viewModel.updateTempEditNoteText(it) },
                        onSave = { newNote, _ ->
                            viewModel.saveNoteWithSentence(
                                character = currentCharacter,
                                note = newNote,
                                includeSentence = false
                            )
                            isEditing = false
                            viewModel.clearTempEditNoteText()
                            viewModel.clearInitializedForCharacter()
                        },
                        onCancel = {
                            isEditing = false
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