package com.example.kostfinder.models

data class User(
    val uid: String = "",
    val name: String = "", // Pastikan field ini ada
    val email: String = "",
    val role: String = "user",
    val favoriteKostIds: List<String> = emptyList()
)
