package com.example.kostfinder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentEmail: String,
    onSaveClick: (String, String) -> Unit,
    onCancelClick: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profil") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                println("Clicked Save with: $name - $email") // debug
                onSaveClick(name, email)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Simpan")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onCancelClick, modifier = Modifier.fillMaxWidth()) {
                Text("Batal")
            }
        }
    }
}
