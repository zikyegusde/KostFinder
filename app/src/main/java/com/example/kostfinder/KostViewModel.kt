package com.example.kostfinder

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.kostfinder.models.Kost
import com.example.kostfinder.models.Rating
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class KostViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _kostList = MutableStateFlow<List<Kost>>(emptyList())
    val kostList = _kostList.asStateFlow()

    private val _promoKosts = MutableStateFlow<List<Kost>>(emptyList())
    val promoKosts = _promoKosts.asStateFlow()

    private val _popularKosts = MutableStateFlow<List<Kost>>(emptyList())
    val popularKosts = _popularKosts.asStateFlow()

    private val _newKosts = MutableStateFlow<List<Kost>>(emptyList())
    val newKosts = _newKosts.asStateFlow()

    private val _selectedKost = MutableStateFlow<Kost?>(null)
    val selectedKost = _selectedKost.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchKostList()
    }

    private fun fetchKostList() {
        _isLoading.value = true
        db.collection("kosts").addSnapshotListener { snapshot, error ->
            if (error != null) {
                _isLoading.value = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val allKosts = snapshot.toObjects(Kost::class.java)
                _kostList.value = allKosts
                _promoKosts.value = allKosts.filter { it.tags.contains("Promo") }
                _newKosts.value = allKosts.sortedByDescending { it.createdAt }

                // --- PERBAIKAN DI SINI: Menggunakan `it.rating` bukan `it["rating"]` ---
                _popularKosts.value = allKosts.sortedByDescending { kost ->
                    if (kost.ratings.isEmpty()) 0.0 else kost.ratings.map { it.rating }.average()
                }
                // ----------------------------------------------------------------------
            }
            _isLoading.value = false
        }
    }

    fun uploadImageToCloudinary(imageUri: Uri, callback: (Result<String>) -> Unit) {
        _isLoading.value = true
        MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) { _isLoading.value = true }
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        callback(Result.success(secureUrl))
                    } else {
                        callback(Result.failure(Exception("URL tidak ditemukan dari Cloudinary.")))
                    }
                    _isLoading.value = false
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    callback(Result.failure(Exception("Gagal mengunggah gambar: ${error.description}")))
                    _isLoading.value = false
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    fun updateKost(kostId: String, updatedKost: Kost, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("kosts").document(kostId).set(updatedKost).await()
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addKost(kost: Kost, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("kosts").add(kost).await()
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteKost(kostId: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("kosts").document(kostId).delete().await()
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getKostById(kostId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            db.collection("kosts").document(kostId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _selectedKost.value = null
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _selectedKost.value = snapshot.toObject(Kost::class.java)
                } else {
                    _selectedKost.value = null
                }
                _isLoading.value = false
            }
        }
    }

    fun addRating(kostId: String, rating: Rating, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("kosts").document(kostId).update("ratings", FieldValue.arrayUnion(rating)).await()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun addAdminReply(kostId: String, ratingToReply: Rating, replyText: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val kostRef = db.collection("kosts").document(kostId)
            try {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(kostRef)
                    val currentKost = snapshot.toObject(Kost::class.java)
                    if (currentKost != null) {
                        val updatedRatings = currentKost.ratings.map { rating ->
                            if (rating.userId == ratingToReply.userId && rating.createdAt == ratingToReply.createdAt) {
                                rating.copy(adminReply = replyText)
                            } else {
                                rating
                            }
                        }
                        transaction.update(kostRef, "ratings", updatedRatings)
                    }
                }.await()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun bookKost(kostId: String, userId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("kosts").document(kostId).update("bookedBy", FieldValue.arrayUnion(userId)).await()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}