package com.example.kostfinder

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostfinder.models.Kost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class KostViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage

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
                val snapshot = db.collection("kosts").get().await()
                _kostList.value = snapshot.toObjects(Kost::class.java)
            } catch (e: Exception) {
                // Tangani error, misalnya dengan log atau pesan ke UI
            } finally {
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
                // Tangani error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addKost(kost: Kost, imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageUrl = uploadImage(imageUri)
                val newKost = kost.copy(imageUrl = imageUrl)
                db.collection("kosts").add(newKost).await()
                fetchKostList() // Refresh list setelah menambah data
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
                fetchKostList() // Refresh list
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        val fileName = "images/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}