package com.example.app.lemolite3

data class Post(
    val postId: String? = null,
    val uid: String? = null,
    val username: String? = null,
    val title: String? = null,
    val caption: String? = null,
    val imageUrl: String? = null,
    val likesCount: Int = 0
)
