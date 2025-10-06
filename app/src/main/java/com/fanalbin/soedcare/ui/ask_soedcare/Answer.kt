//Answer.kt
package com.fanalbin.soedcare.model

import java.util.Date

data class Answer(
    var id: String = "",
    val questionId: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val isDoctor: Boolean = false
)