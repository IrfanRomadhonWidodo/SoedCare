package com.fanalbin.soedcare.model

data class Faskes(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val address: String = "",
    val operatingHours: String = "",
    val phone: String = "",
    val userId: String = "" // Untuk mengaitkan dengan user yang memilih
)