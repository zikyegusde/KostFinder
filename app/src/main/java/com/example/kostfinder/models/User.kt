package com.example.kostfinder.models

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "user",
    val favoriteKostIds: List<String> = emptyList()
)