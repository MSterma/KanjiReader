package com.example.kanjireader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.Repository.KanjiRepository
import com.example.kanjireader.data.local.KanjiDatabase
import com.example.kanjireader.ui.screen.JapaneseTextExtractor
import com.example.kanjireader.ui.theme.KanjiReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(
                applicationContext,
        KanjiDatabase::class.java,
        "kanjidic.db"
        ).createFromAsset("database/kanjidic.db")
        .build()

        val repozytorium = KanjiRepository(database.kanjiDao())
        setContent {
            KanjiReaderTheme {
                val factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return KanjiViewModel(repozytorium) as T
                    }
                }
                val viewModel: KanjiViewModel = viewModel(factory = factory)
                JapaneseTextExtractor(viewModel)
            }
        }

    }
}