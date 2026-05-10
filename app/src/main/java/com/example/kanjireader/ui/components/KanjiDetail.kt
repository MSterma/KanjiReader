package com.example.kanjireader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kanjireader.data.Repository.FullKanjiData

@Composable
fun KanjiDetail(data: FullKanjiData?, onEditClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (data?.dictionaryInfo != null) {
            val info = data.dictionaryInfo

            Text(text = "Kanji:", fontWeight = FontWeight.Bold)
            Text(text = info.character.toString(), fontSize = 48.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Meaning: ${info.meaning}")
            Text(text = "Kun: ${info.kunyomi}")
            Text(text = "On: ${info.onyomi}")

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(text = "My Notes:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = data.userNotes?.userNote?.note ?: "No notes yet.")

            Button(onClick = onEditClick, modifier = Modifier.padding(top = 8.dp)) {
                Text("Edit Note")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Found in sentences:", fontWeight = FontWeight.Bold)
            data.userNotes?.sentences?.forEach { sentence ->
                Text(text = "• ${sentence.sentence}", fontSize = 14.sp)
            }

        } else {
            Text("Select a character from the list")
        }
    }
}