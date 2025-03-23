package com.example.travel.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel.repository.ImageRepository
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class ImageGeneratorViewModel(private val repository: ImageRepository) : ViewModel() {

    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> = _images

    fun generateImages(prompt: String, n: Int) {
        viewModelScope.launch {
            try {
                val result = repository.getImages(prompt, n)
                _images.value = result
            } catch (e: SocketTimeoutException) {
                Log.e("ImageGenerator", "Request timed out: ${e.message}")
                _images.value = emptyList()  // Show empty list on error
            } catch (e: Exception) {
                Log.e("ImageGenerator", "Error occurred: ${e.message}")
                _images.value = emptyList()
            }

        }
    }
}
