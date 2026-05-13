package com.example.kanjireader.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.kanjireader.R
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.ui.screen.JapaneseTextExtractor
import com.example.kanjireader.ui.screen.NotesSearchScreen
import com.example.kanjireader.ui.screen.KanjiSingleDetailScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: KanjiViewModel,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf("extractor") }

    val popupMessage by viewModel.popupMessage.collectAsState()

    BackHandler(enabled = currentScreen != "extractor") {
        if (currentScreen == "detail") {
            currentScreen = "notes"
        } else if (currentScreen == "notes") {
            currentScreen = "extractor"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "KanjiReader",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Text scanner") },
                    selected = currentScreen == "extractor",
                    onClick = {
                        currentScreen = "extractor"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("My notes") },
                    selected = currentScreen == "notes",
                    onClick = {
                        currentScreen = "notes"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text("Log out") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            when (currentScreen) {
                                "extractor" -> "Kanji Scanner"
                                "notes" -> "My notes"
                                "detail" -> "Kanji details"
                                else -> "KanjiReader"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        // Gojmini dodawać ikonę tutaj
                        Image(
                            painter = painterResource(id = R.drawable.appicon),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(60.dp)
                                .padding(end = 8.dp)
                        )
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                when (currentScreen) {
                    "extractor" -> JapaneseTextExtractor(viewModel)
                    "notes" -> NotesSearchScreen(viewModel) { kanji ->
                        viewModel.getChar(kanji)
                        currentScreen = "detail"
                    }
                    "detail" -> KanjiSingleDetailScreen(viewModel) {
                        currentScreen = "notes"
                    }
                }

                AnimatedVisibility(
                    visible = popupMessage != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    popupMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isError) Color(0xFFD32F2F) else Color(0xFF388E3C),
                                contentColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}