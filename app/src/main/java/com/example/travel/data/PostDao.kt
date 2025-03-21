package com.example.travel.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(post: Post)

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE owner = :owner ORDER BY id DESC")
    suspend fun getUserPosts(owner: String): List<Post>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Int): Post?
}
