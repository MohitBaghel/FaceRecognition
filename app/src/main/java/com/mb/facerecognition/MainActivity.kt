package com.mb.facerecognition

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.mb.facerecognition.ui.theme.FaceRecognitionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceRecognitionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FaceCaptureScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    /**
     * Detect faces in the image from the given URI.
     * This function uses ML Kit's Face Detection API.
     */
    fun detectFaces(context: Context, uri: Uri) {
        val image = InputImage.fromFilePath(context, uri)
        val detector = FaceDetection.getClient()

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    Log.d("FaceDetect", "Face found")
                    Toast.makeText(context, "Face detected!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No face found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.e("FaceDetect", "Failed", it)
                Toast.makeText(context, "Face detection failed", Toast.LENGTH_SHORT).show()
            }
    }
}

// Moved outside of class so it can be previewed
@Composable
fun FaceCaptureScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    // Create temp file URI for captured photo
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri.value = photoUri.value
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri.value = it
        }
    }

    fun createImageUri(context: Context): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "face_capture_${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        return contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "Face Recognition Screen",
            style = TextStyle(fontSize = 24.sp, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Camera Button
        Button(onClick = {
            photoUri.value = createImageUri(context)
            photoUri.value?.let { cameraLauncher.launch(it) }
        }) {
            Text("Capture Face from Camera")
        }

        Spacer(Modifier.height(8.dp))

        // Gallery Picker
        Button(onClick = {
            galleryLauncher.launch("image/*")
        }) {
            Text("Pick Image from Gallery")
        }

        Spacer(Modifier.height(16.dp))

        imageUri.value?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                (context as? MainActivity)?.detectFaces(context, uri)
            }) {
                Text("Detect Face")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            openGoogleImageSearch(context)
        }) {
            Text("Search with Google Image")
        }
    }
}

/**
 * Opens Google Images reverse search in the browser.
 */
fun openGoogleImageSearch(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://images.google.com"))
    context.startActivity(intent)
}

/**
 * Preview for Compose UI
 */
@Preview(showBackground = true)
@Composable
fun PreviewFaceCaptureScreen() {
    FaceRecognitionTheme {
        FaceCaptureScreen()
    }
}
