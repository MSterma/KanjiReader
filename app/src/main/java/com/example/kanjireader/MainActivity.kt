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
import com.example.kanjireader.data.local.UserDatabase
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
        val userDatabase = Room.databaseBuilder(
            applicationContext,
            UserDatabase::class.java,
            "user_data.db"
        ).build()

        val repository = KanjiRepository(kanjiDao = database.kanjiDao(),
            userNoteDao = userDatabase.userNoteDao())
        setContent {
            KanjiReaderTheme {
                val factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return KanjiViewModel(repository) as T
                    }
                }
                val viewModel: KanjiViewModel = viewModel(factory = factory)
                JapaneseTextExtractor(viewModel)
            }
        }

    }
}