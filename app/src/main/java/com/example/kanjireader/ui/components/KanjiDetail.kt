package com.example.kanjireader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kanjireader.data.Repository.FullKanjiData

@Composable
fun KanjiDetail(data: FullKanjiData?, onEditClick: () -> Unit) {
    if (data == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Choose from  character from list")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(
                text = data.dictionaryInfo?.character?.toString() ?: "",
                fontSize = 72.sp,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
            )
        }

        item {
            Text("Meaning: ${data.dictionaryInfo?.meaning}", style = MaterialTheme.typography.bodyLarge)
            Text("Onyomi: ${data.dictionaryInfo?.onyomi}")
            Text("Kunyomi: ${data.dictionaryInfo?.kunyomi}")
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Notatka:", style = MaterialTheme.typography.titleMedium)
            Text(text = data.userNotes?.userNote?.note ?: "No notes")
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Found in sentences:", style = MaterialTheme.typography.titleMedium)
        }

        val sentences = data.userNotes?.sentences ?: emptyList()
        if (sentences.isEmpty()) {
            item {
                Text("You have not added sentence to this kanji yet", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            items(sentences) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = item.sentence,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Edit")
            }
        }
    }
}