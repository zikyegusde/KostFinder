package com.example.kostfinder.models

/**
 * Data class to represent a user in Firestore.
 * @property uid The unique ID from Firebase Authentication.
 * @property email The user's email address.
 * @property role The user's role, e.g., "user" or "admin".
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "user" // Default role is "user"
)
