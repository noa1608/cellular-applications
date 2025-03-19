package com.example.travel.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.travel.data.User
import com.example.travel.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun syncUser(userId: String) {
        repository.syncUserFromFirestore(userId)
    }

    fun saveUserToFirestore(user: User) {
        viewModelScope.launch {
            repository.saveUserToFirestore(user)
            repository.insertUserToRoom(user)
        }
    }

    fun insertUserToRoom(user: User) {
        viewModelScope.launch {
            repository.insertUserToRoom(user)
        }
    }

    fun getUserById(userId: String) = viewModelScope.launch {
        val user = repository.getUserById(userId)
    }
    fun getUserByEmail(email: String): LiveData<User?> {
        return repository.getUserByEmail(email)
    }
    fun syncUserWithFirestore(userId: String) {
        viewModelScope.launch {
            repository.syncUserFromFirestore(userId)
        }
    }
}