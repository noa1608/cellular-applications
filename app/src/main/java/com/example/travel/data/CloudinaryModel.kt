package com.example.travel.data

import android.content.Context
import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.example.travel.MyApplication
import java.io.File
import com.example.travel.BuildConfig
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

class CloudinaryModel {

    fun Bitmap.toFile(context: Context, fileName: String): File {
        val file = File(context.cacheDir, "$fileName.jpg")
        Log.d("Cloudinary", "Uploading file: ${file.absolutePath}")
        try {
            // Write the bitmap to the file
            FileOutputStream(file).use { out ->
                this.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }
    fun uploadImage(
        bitmap: Bitmap,
        name: String,
        onSuccess: (String?) -> Unit,
        onError: (String?) -> Unit
    ) {
        val context = MyApplication.Globals.context ?: return
        val file: File = bitmap.toFile(context, name)
        val unsignedPreset = "travel_app_unsigned"

        MediaManager.get().upload(file.path)
            .unsigned(unsignedPreset)
            .callback(object  : UploadCallback {
                override fun onStart(requestId: String?) {

                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {

                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    Log.d("Cloudinary", "Image uploaded successfully: $url")
                    onSuccess(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Error during upload: ${error?.description}")
                    onError(error?.description ?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {

                }

            })
            .dispatch()
    }
}