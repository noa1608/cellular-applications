package com.example.travel.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.travel.data.Post
import com.example.travel.data.PostDao
import com.example.travel.data.User
import com.example.travel.data.firebase.FirebaseService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostRepository(private val postDao: PostDao,private val firebaseService: FirebaseService) {


    fun savePostToFirebase(post: Post, onComplete: (String?) -> Unit) {
        firebaseService.savePost(post) { postId ->
            onComplete(postId)  // Return the generated post ID
        }
    }

    fun insertPost(post: Post): String? {
        try {
            postDao.insertPost(post)
            return post.id
        } catch (e: Exception) {
            return null
        }
    }
    fun createPost(post: Post) {
        firebaseService.savePost(post) { postId ->
            if (postId != null) {
                firebaseService.syncPostFromFirestore(postId) { syncedPost ->
                    syncedPost?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            postDao.insertPost(it)
                        }
                    }
                }
            }
        }
    }
    suspend fun updatePost(post: Post) {
        postDao.updatePost(post)
    }

    suspend fun deletePost(post: Post) {
        postDao.deletePost(post)
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return postDao.getAllPosts()
    }

    suspend fun getUserPosts(owner: String): List<Post> {
        return postDao.getUserPosts(owner)
    }

    suspend fun getPostById(postId: Int): Post? {
        return postDao.getPostById(postId)
    }
}
