package com.example.travel

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.policy.GlobalUploadPolicy

class MyApplication: Application() {

    companion object {
        var isMediaManagerInitialized = false
    }

    object Globals {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        if (!isMediaManagerInitialized) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUD_NAME,
                "api_key" to BuildConfig.API_KEY
            )
            MediaManager.init(this, config)
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.defaultPolicy()

            Globals.context = applicationContext
        }
    }
}