package com.example.kostfinder.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.models.Kost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController, kostViewModel: KostViewModel = viewModel()) {
    var namaKost by remember { mutableStateOf("") }
    var lokasiKost by remember { mutableStateOf("") }
    var deskripsiKost by remember { mutableStateOf("") }
    var hargaKost by remember { mutableStateOf("") }
    var alamatKost by remember { mutableStateOf("") }
    var teleponKost by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("Tersedia") }
    val statusOptions = listOf("Tersedia", "Penuh")

    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("Campur") }
    val kostTypes = listOf("Campur", "Putra", "Putri")

    val kostList by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Text("Panel Admin", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tambah Kost Baru", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = namaKost, onValueChange = { namaKost = it }, label = { Text("Nama Kost") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = lokasiKost, onValueChange = { lokasiKost = it }, label = { Text("Lokasi (Contoh: Jimbaran, Bali)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = hargaKost, onValueChange = { hargaKost = it }, label = { Text("Harga (Contoh: Rp 1.200.000/bulan)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = alamatKost, onValueChange = { alamatKost = it }, label = { Text("Alamat Lengkap") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = teleponKost, onValueChange = { teleponKost = it }, label = { Text("No. Telepon") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = deskripsiKost, onValueChange = { deskripsiKost = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL Gambar") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(text = { Text(status) }, onClick = {
                                    selectedStatus = status
                                    statusExpanded = false
                                })
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipe") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            kostTypes.forEach { type ->
                                DropdownMenuItem(text = { Text(type) }, onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (namaKost.isBlank() || lokasiKost.isBlank() || hargaKost.isBlank() || imageUrl.isBlank()) {
                            Toast.makeText(context, "Semua field wajib diisi.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newKost = Kost(
                            name = namaKost,
                            location = lokasiKost,
                            price = hargaKost,
                            description = deskripsiKost,
                            address = alamatKost,
                            phone = teleponKost,
                            isAvailable = selectedStatus == "Tersedia",
                            imageUrl = imageUrl,
                            type = selectedType
                        )
                        kostViewModel.addKost(newKost) { success, error ->
                            if (success) {
                                Toast.makeText(context, "Kost berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                namaKost = ""; lokasiKost = ""; hargaKost = ""; alamatKost = ""; teleponKost = ""; deskripsiKost = ""; imageUrl = ""
                            } else {
                                Toast.makeText(context, "Gagal menambahkan: $error", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isLoading
                ) {
                    Text("Simpan Kost")
                }

                // PERBAIKAN: Menggunakan HorizontalDivider
                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                Text("Daftar Kost Saat Ini", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading && kostList.isEmpty()) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            } else {
                items(kostList) { kost ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(kost.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(kost.location, style = MaterialTheme.typography.bodyMedium)
                            }
                            Button(
                                onClick = {
                                    kostViewModel.deleteKost(kost.id) { success, error ->
                                        if (success) {
                                            Toast.makeText(context, "Kost dihapus", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gagal menghapus: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                enabled = !isLoading
                            ) {
                                Text("Hapus")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = {
                        Firebase.auth.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
