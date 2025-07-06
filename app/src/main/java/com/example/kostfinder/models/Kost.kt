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
    val promoPrice: String? = null, // Tambahan baru
    val description: String = "",
    val imageUrl: String = "",
    val address: String = "",
    val phone: String = "",
    val ratings: List<Rating> = emptyList(),
    var isAvailable: Boolean = true,
    val bookedBy: List<String> = emptyList(),
    val type: String = "Campur",
    @ServerTimestamp
    val createdAt: Date? = null,
    val tags: List<String> = emptyList()
)