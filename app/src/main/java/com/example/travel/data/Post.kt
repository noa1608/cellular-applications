package com.example.travel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val content: String ="",
    val imagePath: String,
    val owner: String
)
