package com.example.kostfinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostfinder.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // State untuk menyimpan role pengguna saat ini
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole = _userRole.asStateFlow()

    init {
        // Cek role setiap kali status login berubah
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchUserRole(user.uid) { role ->
                    _userRole.value = role
                }
            } else {
                _userRole.value = null
            }
        }
    }

    fun registerUser(
        email: String,
        pass: String,
        role: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val user = User(
                        uid = firebaseUser.uid,
                        email = email,
                        role = role
                    )
                    db.collection("users").document(firebaseUser.uid).set(user).await()
                    callback(true, "Registrasi berhasil!", role)
                } else {
                    callback(false, "Gagal membuat pengguna.", null)
                }

            } catch (e: Exception) {
                callback(false, e.message, null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserRole(uid: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                onComplete(userDoc.getString("role"))
            } catch (e: Exception) {
                onComplete(null)
            }
        }
    }
}