package com.example.kostfinder.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kostfinder.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = Firebase.auth
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (UI Image dan Teks Login tidak berubah)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email dan password wajib diisi"
                    } else {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    // Pengecekan email admin bisa dilakukan di sini jika perlu
                                    if (email.equals("admin@kost.com", ignoreCase = true)) {
                                        navController.navigate("admin") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                } else {
                                    errorMessage = "Login gagal: ${task.exception?.message}"
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Masuk")
            }
        }

        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}