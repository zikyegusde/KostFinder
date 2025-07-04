package com.example.kostfinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostfinder.models.Kost
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
                // PERBAIKAN: Menggunakan toObjects() yang secara otomatis memetakan
                // ID dokumen ke field yang memiliki anotasi @DocumentId.
                _kostList.value = snapshot.toObjects(Kost::class.java)
            }
            _isLoading.value = false
        }
    }

    fun getKostById(kostId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = db.collection("kosts").document(kostId).get().await()
                _selectedKost.value = document.toObject(Kost::class.java)
            } catch (e: Exception) {
                _selectedKost.value = null
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

    fun addRating(kostId: String, rating: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("kosts").document(kostId).update("ratings", FieldValue.arrayUnion(rating)).await()
                getKostById(kostId)
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
                getKostById(kostId)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
