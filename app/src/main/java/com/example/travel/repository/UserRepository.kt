package com.example.travel.repository

import androidx.lifecycle.LiveData
import com.example.travel.data.UserDao
import com.example.travel.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
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
        firestore.collection("users").document(user.id).set(user)
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
}


