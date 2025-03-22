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
    private val _post = MutableLiveData<Post?>()
    private val _postUpdateResult = MutableLiveData<Boolean>()
    private val _postDeleteResult = MutableLiveData<Boolean>()
    val postUpdateResult: LiveData<Boolean> get() = _postUpdateResult
    val postDeleteResult: LiveData<Boolean> get() = _postDeleteResult

    val post: LiveData<Post?> get() = _post

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
                                viewModelScope.launch(Dispatchers.IO) {
                                    postRepository.insertPost(postWithId)
                                }
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

    fun updatePost(post: Post) {
        viewModelScope.launch {
            val result = postRepository.updatePost(post)
            _postUpdateResult.postValue(result)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = postRepository.deletePostById(postId)
            _postDeleteResult.postValue(result)
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


    val postList: LiveData<List<Post>> = postRepository.getAllPosts()


        fun getUserPosts(owner: String) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(postRepository.getUserPosts(owner))
        }

    fun getPostById(postId: String) {
        postRepository.getPostById(postId) { result ->
            _post.postValue(result)
        }
    }

    }
