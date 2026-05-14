package com.example.kanjireader.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.remote.AuthManager

@Composable
fun SettingsScreen(
    viewModel: KanjiViewModel,
    authManager: AuthManager,
    onLogout: () -> Unit
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showClearNotesDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }

    val email = authManager.getCurrentEmail() ?: "Unknown User"
    val dangerColor = Color(0xFFD32F2F)
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val attemptPasswordChange = {
        if (newPassword.length >= 6) {
            showPasswordDialog = true
            focusManager.clearFocus()
        } else {
            viewModel.showMessage("Min. 6 characters required", true)
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = Color(0xFF1E2433),
            title = { Text("Change Password", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure? You will be logged out after success.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordDialog = false
                        isLoading = true
                        authManager.updatePassword(newPassword) { success, _ ->
                            isLoading = false
                            if (success) {
                                viewModel.showMessage("Password changed successfully")
                                onLogout()
                            } else {
                                viewModel.showMessage("Error: couldn't change password", true)
                            }
                        }
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4777))
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPasswordDialog = false },
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearNotesDialog) {
        AlertDialog(
            onDismissRequest = { showClearNotesDialog = false },
            containerColor = Color(0xFF1E2433),
            title = { Text("Clear All Notes", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Delete ALL saved notes? This is permanent.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllNotes()
                        showClearNotesDialog = false
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = dangerColor)
                ) {
                    Text("Clear Notes", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearNotesDialog = false },
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = Color(0xFF1E2433),
            title = { Text("Delete Account", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Delete account? Data will be lost forever.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLocalDataForAccountDeletion()
                        authManager.deleteAccount { success, _ ->
                            if (success) {
                                viewModel.showMessage("Account deleted")
                                onLogout()
                            } else {
                                viewModel.showMessage("Error. Re-login required.", true)
                            }
                        }
                        showDeleteAccountDialog = false
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = dangerColor)
                ) {
                    Text("Delete Account", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAccountDialog = false },
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Text("User: $email")

            Spacer(modifier = Modifier.height(24.dp))
            Text("New Password", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { attemptPasswordChange() })
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showPassword, onCheckedChange = { showPassword = it })
                Text("Show characters")
            }

            Button(
                onClick = attemptPasswordChange,
                shape = RectangleShape,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4777))
            ) {
                Text("Update Password", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Danger Zone", color = dangerColor, style = MaterialTheme.typography.titleMedium)

            OutlinedButton(
                onClick = { showClearNotesDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RectangleShape,
                border = BorderStroke(1.dp, dangerColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = dangerColor)
            ) {
                Text("Clear All Notes")
            }

            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RectangleShape,
                border = BorderStroke(1.dp, dangerColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = dangerColor)
            ) {
                Text("Delete Account")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) { }
            ) {
                ScannerLoadingAnimation(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}