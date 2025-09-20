package com.fanalbin.soedcare.ui.booking

data class QueueNumber(
    val id: String = "",
    val faskesId: String = "",
    val date: String = "",
    val lastNumber: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)