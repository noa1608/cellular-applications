package com.example.travel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicture: String = "")
