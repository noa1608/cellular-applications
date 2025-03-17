package com.example.travel.repository

import androidx.lifecycle.LiveData
import com.example.travel.data.Post
import com.example.travel.data.PostDao

class PostRepository(private val postDao: PostDao) {

    suspend fun insertPost(post: Post) {
        postDao.insertPost(post)
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
