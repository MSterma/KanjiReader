package com.example.kanjireader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.Repository.KanjiRepository
import com.example.kanjireader.data.local.KanjiDatabase
import com.example.kanjireader.data.local.UserDatabase
import com.example.kanjireader.data.remote.AuthManager
import com.example.kanjireader.ui.components.AppNavigation
import com.example.kanjireader.ui.screen.AnimatedSplashScreen
import com.example.kanjireader.ui.screen.LoginScreen
import com.example.kanjireader.ui.theme.KanjiReaderTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kanjireader.ui.screen.SplashViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = android.graphics.Color.parseColor("#12151E")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false

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
                val splashViewModel: SplashViewModel = viewModel()
                var showSplash by remember { mutableStateOf(!splashViewModel.isSplashCompleted) }

                if (showSplash) {
                    AnimatedSplashScreen(
                        onAnimationEnd = {
                            splashViewModel.isSplashCompleted = true
                            showSplash = false
                        }
                    )
                } else {
                    var isLoggedIn by remember { mutableStateOf(authManager.getUserId() != null) }

                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return KanjiViewModel(repository) as T
                        }
                    }
                    val viewModel: KanjiViewModel = viewModel(factory = factory)

                    if (isLoggedIn) {
                        LaunchedEffect(Unit) { viewModel.syncData() }
                        AppNavigation(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.logout {
                                    isLoggedIn = false
                                }
                            },
                            authManager = authManager
                        )
                    } else {
                        LoginScreen(
                            viewModel = viewModel,
                            authManager = authManager,
                            onLoginSuccess = { isLoggedIn = true }
                        )
                    }
                }
            }
        }
    }
}