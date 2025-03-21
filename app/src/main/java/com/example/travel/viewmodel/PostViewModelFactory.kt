package com.example.travel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.travel.data.CloudinaryModel
import com.example.travel.repository.PostRepository

class PostViewModelFactory(
    private val postRepository: PostRepository,
    private val cloudinaryModel: CloudinaryModel
) : ViewModelProvider.Factory {    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(postRepository, cloudinaryModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
