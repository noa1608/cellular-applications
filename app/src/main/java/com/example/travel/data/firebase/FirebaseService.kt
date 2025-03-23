package com.example.travel.data.firebase

import com.example.travel.data.Post
import com.example.travel.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.internal.userAgent

class FirebaseService {

    private val db = FirebaseFirestore.getInstance()

    fun savePost(post: Post, onComplete: (String?) -> Unit) {
        val postMap = hashMapOf(
            "title" to post.title,
            "content" to post.content,
            "imagePath" to post.imagePath,
            "owner" to post.owner
        )
        db.collection("posts")
            .add(postMap)
            .addOnSuccessListener { documentReference ->
                onComplete(documentReference.id)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun syncPostFromFirestore(postId: String, onPostFetched: (Post?) -> Unit) {
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    data?.let {
                        val post = Post(
                            id = document.id,
                            title = it["title"] as? String ?: "",
                            content = it["content"] as? String ?: "",
                            imagePath = it["imagePath"] as? String ?: "",
                            owner = it["owner"] as? String ?: ""
                        )
                        onPostFetched(post)
                    } ?: onPostFetched(null)
                } else {
                    onPostFetched(null)
                }
            }
            .addOnFailureListener {
                onPostFetched(null)
            }
    }
    fun getUserById(userId: String, onUserFetched: (User?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    data?.let {
                        val user = User(
                            id = document.id,
                            username = it["username"] as? String ?: "Unknown",
                            email = it["email"] as? String ?: "No Email"
                        )
                        onUserFetched(user)
                    }
                } else {
                    onUserFetched(null)
                }
            }
            .addOnFailureListener {
                onUserFetched(null)
            }
    }
    fun updatePost(post: Post): Boolean {
        return try {
            val postMap = hashMapOf(
                "title" to post.title,
                "content" to post.content,
                "imagePath" to post.imagePath,
                "owner" to post.owner
            )

            FirebaseFirestore.getInstance().collection("posts")
                .document(post.id)
                .set(postMap) // Update the post
                .addOnSuccessListener {
                    // Post updated successfully in Firebase
                }
                .addOnFailureListener {
                    // Handle failure
                }
            true // Return success
        } catch (e: Exception) {
            false // Return failure
        }
    }
    fun deletePost(postId: String): Boolean {
        return try {
            FirebaseFirestore.getInstance().collection("posts")
                .document(postId)
                .delete()
                .addOnSuccessListener {
                    // Post deleted successfully in Firebase
                }
                .addOnFailureListener {
                    // Handle failure
                }
            true // Return success
        } catch (e: Exception) {
            false // Return failure
        }
    }
    fun fetchAllPosts(onComplete: (List<Post>?) -> Unit) {
        FirebaseFirestore.getInstance().collection("posts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val postList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Post::class.java)?.copy(id = document.id)
                }
                onComplete(postList)
            }
            .addOnFailureListener { e ->
                onComplete(null)
            }
    }
    suspend fun getUserFromFirestore(userId: String): User? {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
