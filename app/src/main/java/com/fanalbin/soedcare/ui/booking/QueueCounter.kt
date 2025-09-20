package com.fanalbin.soedcare.model

data class QueueCounter(
    val faskesId: String = "",
    val date: String = "",
    val counter: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
