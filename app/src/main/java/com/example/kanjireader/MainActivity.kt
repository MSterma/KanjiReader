package com.example.kanjireader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.Repository.KanjiRepository
import com.example.kanjireader.data.local.KanjiDatabase
import com.example.kanjireader.data.local.UserDatabase
import com.example.kanjireader.data.remote.AuthManager
import com.example.kanjireader.ui.screen.JapaneseTextExtractor
import com.example.kanjireader.ui.screen.LoginScreen
import com.example.kanjireader.ui.theme.KanjiReaderTheme
import com.google.firebase.firestore.FirebaseFirestore

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
        val firestore = FirebaseFirestore.getInstance()
        val authManager = AuthManager()

        val repository = KanjiRepository(kanjiDao = database.kanjiDao(),
            userNoteDao = userDatabase.userNoteDao(), authManager=authManager, firestore = firestore)
        setContent {
            KanjiReaderTheme {
                var isLoggedIn by remember {
                    mutableStateOf(authManager.getUserId() != null)
                }

                if (isLoggedIn) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return KanjiViewModel(repository) as T
                        }
                    }
                    val viewModel: KanjiViewModel = viewModel(factory = factory)

                    LaunchedEffect(Unit) {
                        viewModel.syncData()
                    }

                    JapaneseTextExtractor(viewModel)
                } else {
                    LoginScreen(
                        authManager = authManager,
                        onLoginSuccess = { isLoggedIn = true }
                    )
                }
            }
        }
    }
}
