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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kanjireader.R
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.remote.AuthManager
import com.example.kanjireader.ui.screen.JapaneseTextExtractor
import com.example.kanjireader.ui.screen.KanjiSingleDetailScreen
import com.example.kanjireader.ui.screen.NotesSearchScreen
import com.example.kanjireader.ui.screen.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: KanjiViewModel,
    authManager: AuthManager,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf("extractor") }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val popupMessage by viewModel.popupMessage.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val logoSizeDp = if (isTablet) {
        (screenWidthDp * 0.12f).coerceAtMost(80f).dp
    } else {
        (screenWidthDp * 0.10f).coerceAtMost(56f).dp
    }

    val menuIconSize = if (isTablet) 48.dp else 32.dp
    val menuButtonSize = if (isTablet) 56.dp else 48.dp
    val titleFontSize = if (isTablet) 24.sp else 18.sp

    LaunchedEffect(Unit) {
        if (drawerState.currentValue != DrawerValue.Closed) {
            drawerState.close()
        }
    }

    BackHandler(enabled = currentScreen != "extractor") {
        when (currentScreen) {
            "detail" -> currentScreen = "notes"
            "notes" -> currentScreen = "extractor"
            "settings" -> currentScreen = "extractor"
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF1E2433),
            title = { Text("Logout", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        )
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

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentScreen == "settings",
                    onClick = {
                        currentScreen = "settings"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Log out", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color(0xFFD32F2F),
                        unselectedTextColor = Color.White,
                        unselectedIconColor = Color.White
                    )
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
                                "settings" -> "Settings"
                                else -> "KanjiReader"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = titleFontSize,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.size(menuButtonSize)
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier.size(menuIconSize)
                            )
                        }
                    },
                    actions = {
                        Image(
                            painter = painterResource(id = R.drawable.appicon),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(logoSizeDp)
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
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        authManager = authManager,
                        onLogout = onLogout
                    )
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