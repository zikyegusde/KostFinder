package com.example.kostfinder.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedStatus by remember { mutableStateOf("Tersedia") }

    val kostList by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            item {
                Text("Tambah Kost Baru", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                // ... (Semua OutlinedTextField untuk input data, sama seperti sebelumnya)

                OutlinedTextField(value = namaKost, onValueChange = { namaKost = it }, label = { Text("Nama Kost") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = lokasiKost, onValueChange = { lokasiKost = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = hargaKost, onValueChange = { hargaKost = it }, label = { Text("Harga (contoh: Rp 1.200.000/bulan)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = alamatKost, onValueChange = { alamatKost = it }, label = { Text("Alamat Lengkap") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = teleponKost, onValueChange = { teleponKost = it }, label = { Text("No Telepon") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = deskripsiKost, onValueChange = { deskripsiKost = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))


                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Pilih Gambar")
                }
                imageUri?.let {
                    Text("Gambar dipilih: ${it.lastPathSegment}")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val uri = imageUri
                        if (uri == null) {
                            Toast.makeText(context, "Silakan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newKost = Kost(
                            name = namaKost,
                            location = lokasiKost,
                            price = hargaKost,
                            description = deskripsiKost,
                            address = alamatKost,
                            phone = teleponKost,
                            isAvailable = selectedStatus == "Tersedia"
                        )
                        kostViewModel.addKost(newKost, uri) { success, error ->
                            if (success) {
                                Toast.makeText(context, "Kost berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                // Reset fields
                                namaKost = ""
                                lokasiKost = ""
                                hargaKost = ""
                                alamatKost = ""
                                teleponKost = ""
                                deskripsiKost = ""
                                imageUri = null
                            } else {
                                Toast.makeText(context, "Gagal menambahkan: $error", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = namaKost.isNotBlank() && lokasiKost.isNotBlank() && hargaKost.isNotBlank() && !isLoading
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
                        Button(
                            onClick = {
                                kostViewModel.deleteKost(kost.id) { success, error ->
                                    if(success) {
                                        Toast.makeText(context, "Kost dihapus", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Gagal menghapus: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
                        Firebase.auth.signOut()
                        navController.navigate("login") {
                            popUpTo("admin") { inclusive = true }
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