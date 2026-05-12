package com.example.kanjireader.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.remote.AuthManager
import com.google.firebase.FirebaseNetworkException

@Composable
fun LoginScreen(viewModel: KanjiViewModel, authManager: AuthManager, onLoginSuccess: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }

    val popupMessage by viewModel.popupMessage.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = {
                    keyboardController?.hide()
                    if (isRegisterMode) {
                        authManager.signUp(email, password) { success, exception ->
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
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(if (isRegisterMode) "Register" else "Log in")
            }

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(if (isRegisterMode) "Already registered?" else "Register")
            }
        }

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