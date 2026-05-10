package com.example.kanjireader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KanjiList(lista: List<Char>, onClick: (Char) -> Unit) {
    LazyColumn {
        items(lista) { znak ->
            Text(
                text = znak.toString(),
                fontSize = 32.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(znak) }
                    .padding(16.dp)
            )
            Divider()
        }
    }
}