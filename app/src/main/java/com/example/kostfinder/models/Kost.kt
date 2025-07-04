package com.example.kostfinder.models

import com.google.firebase.firestore.DocumentId

data class Kost(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val price: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val address: String = "",
    val phone: String = "",
    val ratings: List<Map<String, Any>> = emptyList(),
    var isAvailable: Boolean = true,
    val bookedBy: List<String> = emptyList(),
    val type: String = "Campur" // Field baru untuk tipe kost (Putra, Putri, Campur)
)
