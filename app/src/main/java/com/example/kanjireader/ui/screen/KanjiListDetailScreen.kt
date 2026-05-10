package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.ui.components.KanjiDetail
import com.example.kanjireader.ui.components.KanjiList

@Composable
fun KanjiListDetailScreen(viewModel: KanjiViewModel, wejscieTekst: String) {
    LaunchedEffect(wejscieTekst) {
        viewModel.processText(wejscieTekst)
    }

    val lista by viewModel.charList.collectAsState()
    val detal by viewModel.selectedChar.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            KanjiList(
                lista = lista,
                onClick = { viewModel.getChar(it) }
            )
        }
        Box(modifier = Modifier.weight(2f)) {
            KanjiDetail(kanji = detal)
        }
    }
}