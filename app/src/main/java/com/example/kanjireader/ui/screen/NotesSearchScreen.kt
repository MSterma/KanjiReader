package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel

@Composable
fun NotesSearchScreen(viewModel: KanjiViewModel, onKanjiClick: (Char) -> Unit) {
    var query by remember { mutableStateOf("") }
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