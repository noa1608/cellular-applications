package com.example.travel.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.liveData
import com.example.travel.data.Post
import com.example.travel.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData

class PostViewModel(private val postRepository: PostRepository) : ViewModel() {
    private val _postInsertResult = MutableLiveData<Boolean>()
    val postInsertResult: LiveData<Boolean> get() = _postInsertResult
    fun insertPost(post: Post) {
        viewModelScope.launch {
            val result = postRepository.insertPost(post) // Assuming insertPost returns a Boolean
            _postInsertResult.postValue(result)  // postValue should be used to update LiveData from background thread
        }
    }

    fun updatePost(post: Post) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        postRepository.updatePost(post)
        emit(Unit)
    }

    fun deletePost(post: Post) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        postRepository.deletePost(post)
        emit(Unit)
    }

    fun getAllPosts(): LiveData<List<Post>> = liveData(Dispatchers.IO) {
        val posts = postRepository.getAllPosts()
        emitSource(posts)
    }


    fun getUserPosts(owner: String) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(postRepository.getUserPosts(owner))
    }

    fun getPostById(postId: Int) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(postRepository.getPostById(postId))  
    }
}
