package com.example.travel.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.travel.data.Post
import com.example.travel.data.PostDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostRepository(private val postDao: PostDao) {

    suspend fun insertPost(post: Post): Int  {
        try {
            postDao.insertPost(post)
            val postId: Long = postDao.insertPost(post)
            return postId.toInt()
        } catch (e: Exception) {
            return 0
        }
    }

    suspend fun updatePost(postId: Long, title: String, content: String) {
        postDao.updatePost(postId, title, content)
    }

    suspend fun deletePostById(postId: Long) {
        postDao.deletePost(postId)
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return postDao.getAllPosts()
    }

    suspend fun getUserPosts(owner: String): List<Post> {
        return postDao.getUserPosts(owner)
    }

    suspend fun getPostById(postId: Long): Post? {
        return postDao.getPostById(postId)
    }
}
