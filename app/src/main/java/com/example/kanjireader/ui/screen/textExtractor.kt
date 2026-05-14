package com.example.kanjireader.ui.screen

import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions

@Composable
fun JapaneseTextExtractor(viewModel: KanjiViewModel) {
    var extractedText by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val recognizer = remember {
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }

    fun processImage(image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isBlank()) {
                    viewModel.showMessage("Sorry, unable to retrieve any text from image", true)
                } else {
                    extractedText = visionText.text
                    viewModel.showMessage("Complete", false)
                }
            }
            .addOnFailureListener { e ->
                extractedText = "Error"
                viewModel.showMessage("Error: ${e.message}", true)
            }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processImage(InputImage.fromBitmap(bitmap, 0))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            processImage(InputImage.fromFilePath(context, uri))
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D4777),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Camera",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Take a picture", fontSize = 16.sp)
                    }
                }

                Button(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D4777),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Choose from gallery", fontSize = 16.sp)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D4777),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Camera",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Take a picture", fontSize = 16.sp)
                    }
                }

                Button(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D4777),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Choose from gallery", fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    KanjiListDetailScreen(viewModel = viewModel, wejscieTekst = extractedText)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (extractedText.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                                Text(
                                    text = "Retrieved text:",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                SelectionContainer {
                                    Text(
                                        text = extractedText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (extractedText.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Retrieved text:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SelectionContainer {
                            Text(
                                text = extractedText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                KanjiListDetailScreen(viewModel = viewModel, wejscieTekst = extractedText)
            }
        }
    }
}