package com.example.travel.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.travel.data.UserDao
import com.example.travel.data.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    fun syncUserFromFirestore(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            userDao.insertUser(it) // Save to Room
                        }
                    }
                }
            }
    }

    suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection("users").document(user.id).set(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to save user to Firestore", e)
        }
    }

    suspend fun insertUserToRoom(user: User) {
        userDao.insertUser(user)
    }
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
    fun getUserByEmail(email: String): LiveData<User?> {
        return userDao.getUserByEmail(email)
    }
    suspend fun uploadProfilePicture(uri: Uri): String? {
        val storageRef = storage.reference
        val profilePicRef = storageRef.child("profile_pictures/${UUID.randomUUID()}.jpg")

        try {
            val uploadTask = profilePicRef.putFile(uri).await() // Upload the image
            val downloadUrl = uploadTask.storage.downloadUrl.await() // Get the download URL
            return downloadUrl.toString() // Return the download URL
        } catch (e: Exception) {
            // Handle upload failure
            Log.e("UserRepository", "Failed to upload profile picture", e)
            return null
        }
    }
}


