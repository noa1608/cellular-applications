package com.example.travel.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ImageRepository {
    suspend fun getImages(prompt: String, n: Int): List<String> = withContext(Dispatchers.IO) {
        val apiKey = "Bearer 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"
        val url = "https://api.openai.com/v1/images/generations"

        val client = OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val requestBody = JSONObject().apply {
            put("prompt", prompt)
            put("n", n)
            put("size", "256x256")
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val imageUrls = mutableListOf<String>()
        val responseBodyString = response.body?.string() ?: ""

        if (response.isSuccessful) {
            val json = JSONObject(responseBodyString)
            val dataArray = json.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val imageUrl = dataArray.getJSONObject(i).getString("url")
                imageUrls.add(imageUrl)
            }
        }
        imageUrls
    }
}
