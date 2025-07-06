package com.example.kostfinder.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Booking(
    val kostId: String = "",
    val kostName: String = "",
    val kostImageUrl: String = "",
    val kostPrice: String = "",
    @ServerTimestamp
    val bookingDate: Date? = null
)