package model

data class User(
    val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val profileImageUrl: String = ""
)