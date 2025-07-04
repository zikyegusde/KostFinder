package com.example.kostfinder.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

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
    val type: String = "Campur",
    // Field baru untuk sesi rekomendasi
    @ServerTimestamp
    val createdAt: Date? = null, // Untuk "Baru Ditambahkan"
    val tags: List<String> = emptyList() // Untuk "Promo Spesial"
)
