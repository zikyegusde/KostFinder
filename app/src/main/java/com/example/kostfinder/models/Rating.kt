package com.example.kostfinder.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Rating(
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    // Field untuk balasan admin
    val adminReply: String = "",
    // Field untuk timestamp, SANGAT PENTING
    @ServerTimestamp
    val createdAt: Date? = null
)