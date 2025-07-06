package com.example.kostfinder

import android.app.Application // ## PERUBAHAN 1: Import Application ##
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel // ## PERUBAHAN 2: Ganti ViewModel menjadi AndroidViewModel ##
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostfinder.models.Booking
import com.example.kostfinder.models.Kost
import com.example.kostfinder.models.User
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

// ## PERUBAHAN 3: Ubah class declaration ##
class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // ## PERUBAHAN 4: Inisialisasi Repository ##
    private val recentlyViewedRepository = RecentlyViewedRepository(application)

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _recentlyViewedIds = MutableStateFlow<List<String>>(emptyList())
    val recentlyViewedIds: StateFlow<List<String>> = _recentlyViewedIds.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchUserData(user.uid)
            } else {
                _userData.value = null
            }
        }
        // ## PERUBAHAN 5: Muat data saat ViewModel dibuat ##
        loadRecentlyViewed()
    }

    private fun fetchUserData(uid: String) {
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

    // ## FUNGSI BARU UNTUK MEMUAT DATA ##
    private fun loadRecentlyViewed() {
        _recentlyViewedIds.value = recentlyViewedRepository.getRecentlyViewed()
    }

    fun addRecentlyViewed(kostId: String) {
        val currentList = _recentlyViewedIds.value.toMutableList()
        currentList.remove(kostId)
        currentList.add(0, kostId)
        val updatedList = currentList.take(10)

        _recentlyViewedIds.value = updatedList
        // ## PERUBAHAN 6: Simpan ke SharedPreferences ##
        recentlyViewedRepository.saveRecentlyViewed(updatedList)
    }

    fun toggleFavorite(kostId: String, context: Context) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Anda harus login untuk menggunakan fitur favorit.", Toast.LENGTH_SHORT).show()
            return
        }
        val userRef = db.collection("users").document(uid)
        viewModelScope.launch {
            try {
                if (_userData.value?.favoriteKostIds?.contains(kostId) == true) {
                    userRef.update("favoriteKostIds", FieldValue.arrayRemove(kostId)).await()
                    Toast.makeText(context, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                } else {
                    userRef.update("favoriteKostIds", FieldValue.arrayUnion(kostId)).await()
                    Toast.makeText(context, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "ERROR: Failed to toggle favorite: ${e.message}")
                Toast.makeText(context, "Gagal memperbarui favorit: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateUserProfile(name: String, email: String, context: Context, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                val userDocRef = db.collection("users").document(user.uid)
                val updates = mapOf(
                    "name" to name,
                    "email" to email
                )
                userDocRef.update(updates).await()
                fetchUserData(user.uid)
                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                onComplete(true)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }

    fun bookKost(kost: Kost, context: Context, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Anda harus login untuk booking.", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        val userRef = db.collection("users").document(user.uid)
        val kostRef = db.collection("kosts").document(kost.id)

        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val newBooking = Booking(
                        kostId = kost.id,
                        kostName = kost.name,
                        kostImageUrl = kost.imageUrl,
                        kostPrice = kost.promoPrice ?: kost.price,
                        bookingDate = Date()
                    )
                    transaction.update(userRef, "bookings", FieldValue.arrayUnion(newBooking))
                    transaction.update(kostRef, "bookedBy", FieldValue.arrayUnion(user.uid))
                }.await()
                Toast.makeText(context, "Booking berhasil!", Toast.LENGTH_SHORT).show()
                onComplete(true)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal melakukan booking: ${e.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }

    fun updateUserProfilePicture(newImageUrl: String, context: Context, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(newImageUrl))
                    .build()
                user.updateProfile(profileUpdates).await()

                db.collection("users").document(user.uid).update("profileImageUrl", newImageUrl).await()
                Toast.makeText(context, "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                onComplete(true)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memperbarui foto: ${e.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }

    fun cancelBooking(booking: Booking, context: Context, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Gagal, pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        val userRef = db.collection("users").document(user.uid)
        val kostRef = db.collection("kosts").document(booking.kostId)

        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    transaction.update(userRef, "bookings", FieldValue.arrayRemove(booking))
                    transaction.update(kostRef, "bookedBy", FieldValue.arrayRemove(user.uid))
                }.await()
                Toast.makeText(context, "Booking berhasil dibatalkan.", Toast.LENGTH_SHORT).show()
                onComplete(true)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membatalkan booking: ${e.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }
}