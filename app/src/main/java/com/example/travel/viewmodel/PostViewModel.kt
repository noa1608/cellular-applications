package com.example.travel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.liveData
import com.example.travel.data.Post
import com.example.travel.repository.PostRepository
import kotlinx.coroutines.Dispatchers

class PostViewModel(private val postRepository: PostRepository) : ViewModel() {

    fun insertPost(post: Post) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        postRepository.insertPost(post)
        emit(Unit)
    }

    fun updatePost(post: Post) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        postRepository.updatePost(post)
        emit(Unit)
    }

    fun deletePost(post: Post) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        postRepository.deletePost(post)
        emit(Unit)
    }

    fun getAllPosts() = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(postRepository.getAllPosts())
    }

    fun getUserPosts(owner: String) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(postRepository.getUserPosts(owner))
    }

    fun getPostById(postId: Int) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(postRepository.getPostById(postId))  
    }
}
