package com.example.travel.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import com.example.travel.data.CloudinaryModel
import com.google.firebase.auth.FirebaseAuth
import java.io.InputStream


class PostViewModel(private val postRepository: PostRepository, private val cloudinaryModel: CloudinaryModel) : ViewModel() {

    private val _postInsertResult = MutableLiveData<String?>()
    val postInsertResult: LiveData<String?> get() = _postInsertResult

    fun createPost(post: Post) {
        viewModelScope.launch {
            postRepository.createPost(post)
        }
    }

    fun createPostWithImage(
        context: Context,
        post: Post,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // Convert Uri to Bitmap
            val bitmap = uriToBitmap(context, imageUri)

            if (bitmap != null) {
                // Upload image to Cloudinary
                cloudinaryModel.uploadImage(bitmap, post.title, { imageUrl ->
                    Log.d("Cloudinary", "Image uploaded successfully: $imageUrl")
                    if (imageUrl != null) {
                        // Create a new post with the Cloudinary image URL
                        val newPost = post.copy(imagePath = imageUrl)  // Copy the post with the updated image path

                        // Save the post in Firebase
                        postRepository.savePostToFirebase(newPost) { postId ->
                            if (postId != null) {
                                // Create a new Post with the updated ID
                                val postWithId = newPost.copy(id = postId)  // Create a new instance with the Firebase-generated ID
                                postRepository.createPost(postWithId)

                                _postInsertResult.postValue(postId)
                                onSuccess(postId)
                            } else {
                                onError("Failed to save post to Firebase.")
                            }
                        }
                    } else {
                        onError("Failed to upload image.")
                    }
                }, { error ->
                    Log.e("Cloudinary", "Image upload failed: $error")
                    onError(error ?: "Unknown error occurred.")
                })
            } else {
                onError("Invalid image.")
            }
        }
    }


    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

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
