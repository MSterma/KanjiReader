package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel

@Composable
fun NotesSearchScreen(viewModel: KanjiViewModel, onKanjiClick: (Char) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val notes by viewModel.userNotes.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAllNotes() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchNotes(it)
            },
            label = { Text("Search notes...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onKanjiClick(note.character.first()) }
                ) {
                    ListItem(
                        headlineContent = { Text(note.character) },
                        supportingContent = { Text(note.note ?: "") }
                    )
                }
            }
        }
    }
}