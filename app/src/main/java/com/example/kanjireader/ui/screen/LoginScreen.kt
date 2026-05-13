package com.example.kanjireader.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kanjireader.R // Upewnij się, że importujesz właściwe R
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.remote.AuthManager
import com.google.firebase.FirebaseNetworkException

@Composable
fun LoginScreen(viewModel: KanjiViewModel, authManager: AuthManager, onLoginSuccess: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }

    // Nowy stan ładowania
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val popupMessage by viewModel.popupMessage.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        // GŁÓWNY CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegisterMode) "Register" else "Log in",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // Blokujemy pola podczas ładowania
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = {
                    keyboardController?.hide()
                    isLoading = true // Start ładowania
                    errorMessage = null

                    if (isRegisterMode) {
                        authManager.signUp(email, password) { success, exception ->
                            isLoading = false // Koniec ładowania
                            if (success) {
                                viewModel.showMessage("Account created successfully", false)
                                onLoginSuccess()
                            } else if (exception is FirebaseNetworkException || exception?.message?.contains("network", true) == true) {
                                viewModel.showMessage("Could not connect to server", true)
                            } else {
                                val errorMsg = exception?.message ?: ""
                                if (errorMsg.contains("email", ignoreCase = true) || errorMsg.contains("format", ignoreCase = true) || errorMsg.contains("empty", ignoreCase = true)) {
                                    errorMessage = "Invalid email"
                                } else {
                                    errorMessage = "Error: Password should be at least 6 characters long."
                                }
                            }
                        }
                    } else {
                        authManager.signIn(email, password) { success, exception ->
                            isLoading = false // Koniec ładowania
                            if (success) {
                                viewModel.showMessage("Logged in successfully", false)
                                onLoginSuccess()
                            } else if (exception is FirebaseNetworkException || exception?.message?.contains("network", true) == true) {
                                viewModel.showMessage("Could not connect to server", true)
                            } else {
                                errorMessage = "Invalid e-mail or password"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !isLoading // Blokujemy przycisk
            ) {
                Text(if (isRegisterMode) "Register" else "Log in")
            }

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = !isLoading
            ) {
                Text(if (isRegisterMode) "Already registered?" else "Register")
            }
        }

        // --- CUSTOM LOADING OVERLAY ---
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)) // Wyszarzenie
                    .clickable(enabled = false) { } // Blokowanie dotyku
            ) {
                ScannerLoadingAnimation(modifier = Modifier.align(Alignment.Center))
            }
        }

        // POPUP (Zawsze na górze)
        AnimatedVisibility(
            visible = popupMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
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

@Composable
fun ScannerLoadingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")

    // Animacja ruchu linii skanującej (od góry do dołu)
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line"
    )

    // Animacja pulsowania logo
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            // LOGO (Pulsowanie)
            Image(
                painter = painterResource(id = R.drawable.appicon ), // Upewnij się, że nazwa pliku się zgadza
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )

            // LINIA SKANUJĄCA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.05f)
                    .offset(y = (150.dp * scanProgress)) // Linia się przesuwa
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Authenticating...",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )

        LinearProgressIndicator(
            modifier = Modifier
                .padding(top = 16.dp)
                .width(100.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}