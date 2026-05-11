// ui/screen/KanjiSingleDetailScreen.kt
package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.ui.components.KanjiDetail
import com.example.kanjireader.ui.components.KanjiNoteEditor

@Composable
fun KanjiSingleDetailScreen(viewModel: KanjiViewModel, onBackClick: () -> Unit) {
    val selectedData by viewModel.selectedData.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Przycisk powrót do wyszukiwarka
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Text("< Back to Notes")
        }

        Box(modifier = Modifier.weight(1f)) {
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