package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorGreyo
import com.a4a.g8invoicing.ui.theme.inputLabel

/**
 * A self-contained logo picker component that handles image selection internally.
 *
 * @param label The label text to display
 * @param selectButtonText Text for the select button
 * @param removeButtonText Accessibility text for remove button
 * @param issuerId The ID of the issuer (used for naming the saved file)
 * @param currentLogoPath The current logo path (relative)
 * @param onLogoPathChanged Callback when the logo path changes (null when removed)
 * @param errorTitle Title for error dialog
 * @param errorDismissText Dismiss button text for error dialog
 * @param modifier Modifier for the component
 */
@Composable
fun LogoPickerComponent(
    label: String,
    selectButtonText: String,
    removeButtonText: String,
    issuerId: Int?,
    currentLogoPath: String?,
    onLogoPathChanged: (String?) -> Unit,
    errorTitle: String = "Erreur",
    errorDismissText: String = "OK",
    modifier: Modifier = Modifier
) {
    // Initialize platform-specific context (Android needs Context)
    InitImageContext()

    val imageStorage = remember { ImageStorage() }
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load existing logo bitmap when path changes
    LaunchedEffect(currentLogoPath) {
        logoBitmap = if (currentLogoPath != null && imageStorage.logoExists(currentLogoPath)) {
            loadLogoBitmap(imageStorage.getAbsolutePath(currentLogoPath))
        } else {
            null
        }
    }

    // Image picker launcher
    val imagePicker = rememberImagePickerLauncher { result ->
        when (result) {
            is ImagePickerResult.Success -> {
                // Save the image and update the path
                val effectiveIssuerId = issuerId ?: 0
                val saveResult = imageStorage.saveLogoImageWithValidation(result.path, effectiveIssuerId)

                when (saveResult) {
                    is LogoSaveResult.Success -> {
                        onLogoPathChanged(saveResult.path)
                        logoBitmap = loadLogoBitmap(imageStorage.getAbsolutePath(saveResult.path))
                    }
                    is LogoSaveResult.ImageTooLarge -> {
                        errorMessage = "Image trop grande : ${saveResult.width}x${saveResult.height} pixels.\n\n" +
                                "Dimensions max : ${saveResult.maxWidth}x${saveResult.maxHeight} pixels.\n\n" +
                                "Veuillez redimensionner l'image avant de l'importer."
                    }
                    is LogoSaveResult.FileTooLarge -> {
                        errorMessage = "Fichier trop lourd : ${saveResult.sizeKb} Ko.\n\n" +
                                "Taille max : ${saveResult.maxSizeKb} Ko.\n\n" +
                                "Veuillez compresser l'image avant de l'importer."
                    }
                    is LogoSaveResult.InvalidFormat -> {
                        errorMessage = "Format non supporté : ${saveResult.format}.\n\n" +
                                "Formats acceptés : PNG, JPG, JPEG."
                    }
                    is LogoSaveResult.Error -> {
                        errorMessage = "Erreur lors de l'importation du logo.\n\n${saveResult.message}"
                    }
                }
            }
            is ImagePickerResult.Cancelled -> {
                // Do nothing
            }
            is ImagePickerResult.Error -> {
                errorMessage = "Erreur : ${result.message}"
            }
        }
    }

    // Error dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text(errorTitle) },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text(errorDismissText, color = Color.Black)
                }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Label
        Text(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(end = 3.dp),
            text = label,
            style = MaterialTheme.typography.inputLabel
        )

        // Logo area
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            if (currentLogoPath != null && logoBitmap != null) {
                // Display filename chip with remove button
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentLogoPath.substringAfterLast("/"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(
                        onClick = {
                            // Delete the logo file
                            imageStorage.deleteLogo(currentLogoPath)
                            logoBitmap = null
                            onLogoPathChanged(null)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = removeButtonText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // Show add logo button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, ColorGreyo.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .clickable { imagePicker.launch() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        tint = ColorGreyo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectButtonText,
                        style = MaterialTheme.typography.inputLabel,
                        color = ColorGreyo
                    )
                }
            }
        }
    }
}

/**
 * Platform-specific function to load a logo bitmap from file path.
 */
expect fun loadLogoBitmap(absolutePath: String): ImageBitmap?
