package com.example.kostfinder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val user = Firebase.auth.currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Display user info from Firebase Auth
            Text(user?.displayName ?: "Pengguna", style = MaterialTheme.typography.headlineMedium)
            Text(user?.email ?: "Tidak ada email", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onEditProfileClick, modifier = Modifier.fillMaxWidth()) {
                Text("Edit Profil")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    Firebase.auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true } // Clear back stack
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}
