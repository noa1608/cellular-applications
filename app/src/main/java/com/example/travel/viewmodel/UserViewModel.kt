package com.example.travel.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.travel.data.User
import com.example.travel.repository.UserRepository
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.lifecycle.MutableLiveData


class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _createUserStatus = MutableLiveData<Result<Unit>>()
    val createUserStatus: LiveData<Result<Unit>> = _createUserStatus

    fun createUser(user: User) {
        viewModelScope.launch {
            try {
                repository.saveUserToFirestore(user)
                repository.syncUserFromFirestore(user.id)
                _createUserStatus.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _createUserStatus.postValue(Result.failure(e))
            }
        }
    }
    fun syncUser(userId: String) {
        repository.syncUserFromFirestore(userId)
    }

    fun getUserById(userId: String) = viewModelScope.launch {
        val user = repository.getUserById(userId)
    }
    fun getUserByEmail(email: String): LiveData<User?> {
        return repository.getUserByEmail(email)
    }
    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

}