package com.example.kostfinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostfinder.models.Kost
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

    fun fetchKostList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Menggunakan listener Firestore untuk pembaruan realtime
                db.collection("kosts").addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        _kostList.value = snapshot.toObjects(Kost::class.java)
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun getKostById(kostId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = db.collection("kosts").document(kostId).get().await()
                _selectedKost.value = document.toObject(Kost::class.java)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Menambahkan objek Kost baru ke Firestore.
     * Objek Kost diasumsikan sudah berisi URL gambar.
     */
    fun addKost(kost: Kost, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("kosts").add(kost).await()
                // Tidak perlu fetch ulang karena listener sudah aktif
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
}
