package com.example.kanjireader.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kanjireader.data.Model.KanjiInfo

@Composable
fun KanjiDetail(kanji: KanjiInfo?) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (kanji != null) {
            Text(text = "Znak:", fontWeight = FontWeight.Bold)
            Text(text = kanji.character.toString(), fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Znaczenie:", fontWeight = FontWeight.Bold)
            Text(text = kanji.meaning)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Kun'yomi:", fontWeight = FontWeight.Bold)
            Text(text = kanji.kunyomi)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "On'yomi:", fontWeight = FontWeight.Bold)
            Text(text = kanji.onyomi)
        } else {
            Text("Wybrać znak")
        }
    }
}