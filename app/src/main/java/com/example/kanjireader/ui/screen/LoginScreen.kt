// ui/screen/LoginScreen.kt
package com.example.kanjireader.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kanjireader.data.remote.AuthManager

@Composable
fun LoginScreen(authManager: AuthManager, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isRegisterMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegisterMode) "Rejestracja Kanjireader" else "Logowanie Kanjireader",
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
            label = { Text("Hasło") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                if (isRegisterMode) {
                    authManager.signUp(email, password) { success ->
                        if (success) onLoginSuccess()
                        else errorMessage = "Błąd rejestracja. Hasło musieć mieć 6 znak minimum."
                    }
                } else {
                    authManager.signIn(email, password) { success ->
                        if (success) onLoginSuccess()
                        else errorMessage = "Błędny email lub hasło"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text(if (isRegisterMode) "Zarejestruj" else "Zaloguj")
        }

        TextButton(
            onClick = {
                isRegisterMode = !isRegisterMode
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(if (isRegisterMode) "Masz już konto? Zaloguj się" else "Nie masz konta? Zarejestruj się")
        }
    }
}