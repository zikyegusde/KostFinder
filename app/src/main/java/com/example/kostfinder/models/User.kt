package com.example.kostfinder.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val profileImageUrl: String? = null,
    val favoriteKostIds: List<String> = emptyList(),
    val bookings: List<Booking> = emptyList()
)