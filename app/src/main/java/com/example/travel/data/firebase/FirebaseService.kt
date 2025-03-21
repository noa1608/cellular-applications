package com.example.travel.data.firebase

import com.example.travel.data.Post
import com.example.travel.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
}
