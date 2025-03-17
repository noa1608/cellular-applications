package com.example.travel.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun saveImageToSharedDirectory(imageUri: Uri, context: Context): String? {
    val fileName = "post_${System.currentTimeMillis()}.jpg"
    val directory = File(context.filesDir, "post_images")

    if (!directory.exists()) {
        directory.mkdirs() // Create the directory if it doesn't exist
    }

    val file = File(directory, fileName)

    return try {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        file.absolutePath // Return the saved image path
    } catch (e: IOException) {
        e.printStackTrace()
        null // Return null if an error occurs
    }
}
