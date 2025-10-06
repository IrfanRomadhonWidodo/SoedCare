//Notification.kt
package com.fanalbin.soedcare.model

import java.util.Date

data class Notification(
    val id: String = "",
    val userId: String = "",
    val questionId: String = "",
    val questionTitle: String = "",
    val answerContent: String = "",
    val answeredBy: String = "",
    val isDoctor: Boolean = false,
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    var isSelected: Boolean = false  // Tambahkan properti ini
)