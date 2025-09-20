// Booking.kt
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
    val userAddress: String = "",
    val bookingDate: String = "",
    val bookingTime: String = "",
    val notes: String = "",
    val queueNumber: Int = 0,
    val queueNumberFormatted: String = "",
    val status: String = "confirmed",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable