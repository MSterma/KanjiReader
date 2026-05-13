package com.example.kanjireader.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kanjireader.R
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.example.kanjireader.data.remote.AuthManager
import com.google.firebase.FirebaseNetworkException

@Composable
fun LoginScreen(viewModel: KanjiViewModel, authManager: AuthManager, onLoginSuccess: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val popupMessage by viewModel.popupMessage.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val bgColor = Color(0xFF12151E)
    val fieldColor = Color(0xFF1E2433)
    val textColor = Color.White
    val labelColor = Color.LightGray
    val accentColor = Color(0xFF1D4777)

    // Gojmini wyodrębniać logika autoryzacja
    val performAuth = {
        if (!isLoading) {
            keyboardController?.hide()
            focusManager.clearFocus()
            isLoading = true
            errorMessage = null

            if (isRegisterMode) {
                authManager.signUp(email, password) { success, exception ->
                    isLoading = false
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
                    isLoading = false
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
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.appicon),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = if (isRegisterMode) "Register" else "Log in",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            val textFieldColors = TextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedContainerColor = fieldColor,
                unfocusedContainerColor = fieldColor,
                focusedLabelColor = labelColor,
                unfocusedLabelColor = labelColor,
                cursorColor = textColor
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { performAuth() })
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = performAuth,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !isLoading,
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(if (isRegisterMode) "Register" else "Log in", color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = !isLoading,
                shape = RectangleShape,
                border = BorderStroke(1.dp, accentColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = bgColor,
                    contentColor = textColor
                )
            ) {
                Text(if (isRegisterMode) "Already registered? Log in" else "Register")
            }
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
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.appicon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.08f)
                    .offset(y = (150.dp * scanProgress) - (150.dp * 0.04f))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent)
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Authenticating...", color = Color.White, style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            modifier = Modifier.padding(top = 16.dp).width(120.dp).clip(RoundedCornerShape(4.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}