package com.example.kostfinder

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostfinder.models.User
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    init {
        // Secara proaktif memuat data pengguna saat status autentikasi berubah (login/logout)
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchUserData(user.uid)
            } else {
                // Mengosongkan data jika pengguna logout
                _userData.value = null
            }
        }
    }

    private fun fetchUserData(uid: String) {
        // Menggunakan listener agar UI favorit selalu sinkron dengan database
        db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserViewModel", "Error fetching user data: ", error)
                _userData.value = null
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                _userData.value = snapshot.toObject(User::class.java)
            }
        }
    }

    fun toggleFavorite(kostId: String, context: Context) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Anda harus login untuk menggunakan fitur favorit.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentFavorites = _userData.value?.favoriteKostIds ?: emptyList()
        val userRef = db.collection("users").document(uid)

        viewModelScope.launch {
            try {
                if (currentFavorites.contains(kostId)) {
                    // Jika sudah ada, hapus dari favorit
                    userRef.update("favoriteKostIds", FieldValue.arrayRemove(kostId)).await()
                    Log.d("UserViewModel", "SUCCESS: Removed $kostId from favorites")
                    Toast.makeText(context, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                } else {
                    // Jika belum ada, tambahkan ke favorit
                    userRef.update("favoriteKostIds", FieldValue.arrayUnion(kostId)).await()
                    Log.d("UserViewModel", "SUCCESS: Added $kostId to favorites")
                    Toast.makeText(context, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Menampilkan pesan error jika pembaruan gagal
                Log.e("UserViewModel", "ERROR: Failed to toggle favorite: ${e.message}")
                Toast.makeText(context, "Gagal memperbarui favorit: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- TAMBAHKAN FUNGSI BARU DI SINI ---
    fun updateUserProfile(name: String, email: String, context: Context, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                // 1. Memperbarui nama di Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                // 2. Memperbarui nama dan email di Firestore
                val userDocRef = db.collection("users").document(user.uid)
                val updates = mapOf(
                    "name" to name,
                    "email" to email
                )
                userDocRef.update(updates).await()

                // 3. Memuat ulang data pengguna agar UI ter-update
                fetchUserData(user.uid)

                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                onComplete(true)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }
}