package com.fanalbin.soedcare.model

import java.util.Date

data class Question(
    var id: String = "",
    val userId: String = "",
    var userName: String = "", // Ubah val menjadi var
    val title: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val likes: Int = 0,
    val likedBy: List<String> = listOf(), // List of user IDs who liked this question
    val replyCount: Int = 0,
    val answeredByDoctor: Boolean = false
)