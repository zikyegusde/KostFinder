package com.example.kostfinder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kostfinder.data.kostList
import com.example.kostfinder.models.Kost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    var namaKost by remember { mutableStateOf("") }
    var lokasiKost by remember { mutableStateOf("") }
    var deskripsiKost by remember { mutableStateOf("") }
    var hargaKost by remember { mutableStateOf("") }
    var alamatKost by remember { mutableStateOf("") }
    var teleponKost by remember { mutableStateOf("") }
    var gambarKost by remember { mutableStateOf("") }

    // ✅ Status dropdown
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("Tersedia") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Text("Tambah Kost Baru", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = namaKost,
                onValueChange = { namaKost = it },
                label = { Text("Nama Kost") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lokasiKost,
                onValueChange = { lokasiKost = it },
                label = { Text("Lokasi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = hargaKost,
                onValueChange = { hargaKost = it },
                label = { Text("Harga (contoh: Rp 1.200.000/bulan)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = alamatKost,
                onValueChange = { alamatKost = it },
                label = { Text("Alamat Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = teleponKost,
                onValueChange = { teleponKost = it },
                label = { Text("No Telepon") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = gambarKost,
                onValueChange = { gambarKost = it },
                label = { Text("URL Gambar") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = deskripsiKost,
                onValueChange = { deskripsiKost = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Dropdown Status
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Kos") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tersedia") },
                        onClick = {
                            selectedStatus = "Tersedia"
                            statusExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Penuh") },
                        onClick = {
                            selectedStatus = "Penuh"
                            statusExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newId = if (kostList.isNotEmpty()) kostList.maxOf { it.id } + 1 else 1
                    kostList.add(
                        Kost(
                            id = newId,
                            name = namaKost,
                            location = lokasiKost,
                            price = hargaKost,
                            description = deskripsiKost,
                            imageUrl = gambarKost,
                            address = alamatKost,
                            phone = teleponKost,
                            isAvailable = selectedStatus == "Tersedia" // ✅ Simpan status
                        )
                    )
                    namaKost = ""
                    lokasiKost = ""
                    hargaKost = ""
                    alamatKost = ""
                    teleponKost = ""
                    gambarKost = ""
                    deskripsiKost = ""
                    selectedStatus = "Tersedia"
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = namaKost.isNotBlank() && lokasiKost.isNotBlank() && hargaKost.isNotBlank()
            ) {
                Text("Simpan")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Daftar Kost", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(kostList) { kost ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Nama: ${kost.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Lokasi: ${kost.location}")
                    Text("Harga: ${kost.price}")
                    Text("Status: ${if (kost.isAvailable) "Tersedia" else "Penuh"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            kostList.remove(kost)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Hapus", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("admin") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kembali ke Login")
            }
        }
    }
}
