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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                            initialNote = selectedData?.userNotes?.userNote?.note ?: "",
                            currentSentence = "",
                            onSave = { newNote, _ ->
                                viewModel.saveNoteWithSentence(
                                    character = selectedData!!.dictionaryInfo!!.character.toString(),
                                    note = newNote,
                                    includeSentence = false
                                )
                                isEditing = false
                            },
                            onCancel = { isEditing = false }
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                if (isEditing && selectedData != null) {
                    KanjiNoteEditor(
                        initialNote = selectedData?.userNotes?.userNote?.note ?: "",
                        currentSentence = "",
                        onSave = { newNote, _ ->
                            viewModel.saveNoteWithSentence(
                                character = selectedData!!.dictionaryInfo!!.character.toString(),
                                note = newNote,
                                includeSentence = false
                            )
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
}