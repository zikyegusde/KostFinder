package com.example.kostfinder.models

data class Kost(
    val id: Int,
    val name: String,
    val location: String,
    val price: String,
    val description: String,
    val imageUrl: String,
    val address: String,
    val phone: String,
    val ratings: MutableList<Pair<Int, String>> = mutableListOf(),
    var isAvailable: Boolean = true
)
