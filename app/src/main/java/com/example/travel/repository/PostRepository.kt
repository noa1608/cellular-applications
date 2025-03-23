package com.example.travel.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.travel.data.Post
import com.example.travel.data.PostDao
import com.example.travel.data.User
import com.example.travel.data.firebase.FirebaseService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostRepository(private val postDao: PostDao,private val firebaseService: FirebaseService) {

    fun syncAllPostsFromFirebase(onComplete: (Boolean) -> Unit) {
        firebaseService.fetchAllPosts { firebasePosts ->
            if (firebasePosts != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    postDao.insertPosts(firebasePosts)
                    onComplete(true)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun savePostToFirebase(post: Post, onComplete: (String?) -> Unit) {
        firebaseService.savePost(post) { postId ->
            onComplete(postId)
        }
    }

    suspend fun insertPost(post: Post): String? {
        postDao.insertPost(post)
        return post.id
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
    suspend fun updatePost(post: Post): Boolean {
        return try {
            // Update post in Firebase
            val firebaseUpdateResult = firebaseService.updatePost(post)
            if (firebaseUpdateResult) {
                // Update post in Room if Firebase update is successful
                postDao.updatePost(post)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    suspend fun deletePostById(postId: String): Boolean {
        return try {
            // Delete post from Firebase
            val firebaseDeleteResult = firebaseService.deletePost(postId)
            if (firebaseDeleteResult) {
                // Delete post from Room if Firebase deletion is successful
                postDao.deletePost(postId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return postDao.getAllPosts()
    }

    suspend fun getUserPosts(owner: String): List<Post> {
        return postDao.getUserPosts(owner)
    }

    fun getPostById(postId: String, onPostFetched: (Post?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                var post = postDao.getPostById(postId)
                if (post != null) {
                    onPostFetched(post)  // If found in Room, return it
                } else {
                    firebaseService.syncPostFromFirestore(postId) { firebasePost ->
                        firebasePost?.let {
                            CoroutineScope(Dispatchers.IO).launch {
                                postDao.insertPost(it)  // Save to Room for future use
                            }
                        }
                        onPostFetched(firebasePost)  // Return Firebase data
                    }
                }
            }    }

}
