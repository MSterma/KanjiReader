package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel

@Composable
fun NotesSearchScreen(viewModel: KanjiViewModel, onKanjiClick: (Char) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val notes by viewModel.userNotes.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var characterToDelete by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadAllNotes() }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E2433),
            title = { Text("Delete Note", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the note for '$characterToDelete'?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote(characterToDelete)
                        showDeleteDialog = false
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchNotes(it)
            },
            label = { Text("Search notes...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
            })
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onKanjiClick(note.character.first())
                    }
                ) {
                    ListItem(
                        headlineContent = { Text(note.character) },
                        supportingContent = { Text(note.meaning) },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    characterToDelete = note.character
                                    showDeleteDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}