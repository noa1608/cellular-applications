package com.example.travel.data.firebase

import com.example.travel.data.Post
import com.google.firebase.firestore.FirebaseFirestore

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
}
