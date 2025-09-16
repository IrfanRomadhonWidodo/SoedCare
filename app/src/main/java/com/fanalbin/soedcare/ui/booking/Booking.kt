package com.fanalbin.soedcare.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Booking(
    val id: String = "",
    val userId: String = "",
    val faskesId: String = "",
    val faskesName: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userAddress: String = "", // Tambahkan field address
    val bookingDate: String = "",
    val bookingTime: String = "",
    val notes: String = "",
    val queueNumber: Int = 0,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable