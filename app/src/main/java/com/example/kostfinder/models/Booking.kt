package com.example.kostfinder.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Booking(
    @DocumentId
    val id: String = "", // ID unik dari koleksi bookings
    val kostId: String = "",
    val kostName: String = "",
    val kostImageUrl: String = "",
    val kostPrice: String = "",

    // Info Penyewa
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",

    // Status Booking
    var status: String = "Pending", // "Pending", "Approved", "Rejected"
    var rejectionMessage: String? = null,
    var adminPaymentDetails: String? = null, // Diisi oleh admin saat menyetujui

    @ServerTimestamp
    val bookingDate: Date? = null
)