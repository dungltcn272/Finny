package com.ltcn272.finny.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.features.snackbar.SnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import java.io.File
import java.io.FileOutputStream

/**
 * Creates a temporary file from a content URI.
 * Handles exceptions and shows a snackbar message on failure.
 *
 * @param context The application context.
 * @param uri The content URI of the file to be copied.
 * @param snackbarManager The manager to show snackbar messages.
 * @return The temporary File object, or null if creation failed.
 */
fun createFileFromUri(context: Context, uri: Uri, snackbarManager: SnackbarManager): File? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        }
    } catch (e: Exception) {
        Log.e("FileUtils", "Error creating temp file from URI: ${e.message}", e)
        snackbarManager.showMessage(
            context.getString(R.string.error_read_image_file),
            TopSnackbarType.ERROR
        )
        null
    }
}
