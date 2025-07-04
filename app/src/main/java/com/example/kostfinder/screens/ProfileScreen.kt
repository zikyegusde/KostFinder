package com.example.kostfinder.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // <-- PERBAIKAN: Tambahkan import ini
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
                title = { Text("Profil Saya") },
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
                .fillMaxSize()
        ) {
            // User Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    user?.displayName ?: "Pengguna",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    user?.email ?: "Tidak ada email",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onEditProfileClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit Profil")
                }
            }

            Divider()

            // Menu Section
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem(
                    text = "Tentang Aplikasi",
                    icon = Icons.Default.Info,
                    onClick = { navController.navigate("about") }
                )
                ProfileMenuItem(
                    text = "Syarat dan Ketentuan",
                    icon = Icons.Default.Description,
                    onClick = { navController.navigate("terms") }
                )
                ProfileMenuItem(
                    text = "Kebijakan Privasi",
                    icon = Icons.Default.Shield,
                    onClick = { navController.navigate("privacy_policy") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button at the bottom
            OutlinedButton(
                onClick = {
                    Firebase.auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true } // Clear back stack
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileMenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "Go to $text",
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
    }
}