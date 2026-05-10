package com.example.kanjireader.ui.screen

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kanjireader.ViewModel.KanjiViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions

@Composable
fun JapaneseTextExtractor(viewModel: KanjiViewModel) {
    var extractedText by remember { mutableStateOf("Brak tekst") }
    val context = LocalContext.current

    val recognizer = remember {
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }

    fun processImage(image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                extractedText = visionText.text.ifEmpty { "Nie znaleźć znaki" }
            }
            .addOnFailureListener { e ->
                extractedText = "Błąd: ${e.message}"
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

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { cameraLauncher.launch(null) }) {
                Text("Zrobić zdjęcie")
            }
            Button(onClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Wybrać galeria")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SelectionContainer {
           KanjiListDetailScreen(viewModel = viewModel,extractedText)
        }
    }
}