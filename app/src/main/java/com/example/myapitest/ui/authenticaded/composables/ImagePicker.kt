package com.example.myapitest.ui.authenticaded.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapitest.network.FirebaseService
import com.example.myapitest.ui.theme.MyApiTestTheme
import kotlinx.coroutines.delay

@Composable
fun ImagePicker(
    modifier: Modifier = Modifier,
    currentImageUrl: String? = null,
    folder: String = "images",
    onImageSelected: (String) -> Unit = {},
    onImageRemoved: () -> Unit = {},
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(3000)
            showSuccessMessage = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedImageUri = selectedUri
            isUploading = true
            errorMessage = null
            showSuccessMessage = false

            FirebaseService.uploadImage(
                imageUri = selectedUri,
                folder = folder,
                onProgress = { progress ->
                    uploadProgress = progress
                },
                onComplete = { success, result ->
                    isUploading = false
                    if (success && result != null) {
                        onImageSelected(result)
                        selectedImageUri = null
                        showSuccessMessage = true
                    } else {
                        errorMessage = result ?: "Upload failed"
                        selectedImageUri = null
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier.background(color = Color.Transparent).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSuccessMessage) {
            Text(
                text = "Image uploaded successfully!",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isUploading) {
            LinearProgressIndicator(
                progress = { uploadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (!isUploading) {
                        imagePickerLauncher.launch("image/*")
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isUploading
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (currentImageUrl != null) "Change" else "Select")
            }

            if (currentImageUrl != null) {
                IconButton(
                    onClick = onImageRemoved,
                    enabled = !isUploading
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove image",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultImagePlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add image",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to select image",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePickerPreview(modifier: Modifier = Modifier) {
    MyApiTestTheme {
        ImagePicker()
    }
}