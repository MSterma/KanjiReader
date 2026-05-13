package com.example.kanjireader.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun KanjiNoteEditor(
    initialNote: String,
    currentSentence: String,
    onSave: (String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initialNote) }
    var includeSentence by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Edit", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            label = { Text("Your note") }
        )

        if (currentSentence.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = includeSentence,
                    onCheckedChange = { includeSentence = it }
                )
                Text("Save sentence", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = currentSentence,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onSave(text, includeSentence) },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4777))
            ) {
                Text("Save", color = Color.White)
            }
            OutlinedButton(
                onClick = onCancel,
                shape = RectangleShape,
                border = BorderStroke(1.dp, Color(0xFF1D4777)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1D4777))
            ) {
                Text("Cancel")
            }
        }
    }
}