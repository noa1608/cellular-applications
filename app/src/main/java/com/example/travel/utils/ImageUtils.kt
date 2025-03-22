package com.example.travel.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// For saving post images
fun savePostImageToDirectory(imageUri: Uri, context: Context): String? {
    return saveImageToDirectory(imageUri, context, "post_images", "post")
}

// Shared logic
private fun saveImageToDirectory(imageUri: Uri, context: Context, folderName: String, prefix: String): String? {
    val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
    val directory = File(context.filesDir, folderName)

    if (!directory.exists()) {
        directory.mkdirs()
    }

    val file = File(directory, fileName)

    return try {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
