package com.example.kostfinder.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.models.Kost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditKostScreen(
    navController: NavController,
    kostId: String,
    kostViewModel: KostViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by kostViewModel.isLoading.collectAsState()
    val selectedKost by kostViewModel.selectedKost.collectAsState()

    var namaKost by remember { mutableStateOf("") }
    var deskripsiKost by remember { mutableStateOf("") }
    var hargaKost by remember { mutableStateOf("") }
    var alamatKost by remember { mutableStateOf("") }
    var teleponKost by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedKabupaten by remember { mutableStateOf("Denpasar") }
    var selectedStatus by remember { mutableStateOf("Tersedia") }
    var selectedType by remember { mutableStateOf("Campur") }
    var isPromo by remember { mutableStateOf(false) }
    var hargaPromo by remember { mutableStateOf("") } // Tambahan baru

    var periodeExpanded by remember { mutableStateOf(false) }
    var selectedPeriode by remember { mutableStateOf("Bulan") }
    val periodeOptions = listOf("Bulan", "Tahun")

    var kabupatenExpanded by remember { mutableStateOf(false) }
    val kabupatenOptions = listOf("Badung", "Bangli", "Buleleng", "Denpasar", "Gianyar", "Jembrana", "Karangasem", "Klungkung", "Tabanan")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    LaunchedEffect(key1 = kostId) {
        kostViewModel.getKostById(kostId)
    }

    LaunchedEffect(key1 = selectedKost) {
        selectedKost?.let { kost ->
            namaKost = kost.name
            deskripsiKost = kost.description

            val priceParts = kost.price.split("/")
            hargaKost = priceParts.getOrNull(0)?.filter { it.isDigit() } ?: ""
            selectedPeriode = priceParts.getOrNull(1)?.trim() ?: "Bulan"

            // Perubahan: Muat data harga promo
            hargaPromo = kost.promoPrice?.filter { it.isDigit() } ?: ""

            alamatKost = kost.address
            teleponKost = kost.phone
            existingImageUrl = kost.imageUrl
            selectedKabupaten = kost.location
            selectedStatus = if (kost.isAvailable) "Tersedia" else "Penuh"
            selectedType = kost.type
            isPromo = kost.tags.contains("Promo")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Kost") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && selectedKost == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = namaKost, onValueChange = { namaKost = it }, label = { Text("Nama Kost") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = kabupatenExpanded,
                    onExpandedChange = { kabupatenExpanded = !kabupatenExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedKabupaten,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kabupaten") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kabupatenExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = kabupatenExpanded,
                        onDismissRequest = { kabupatenExpanded = false }
                    ) {
                        kabupatenOptions.forEach { kabupaten ->
                            DropdownMenuItem(
                                text = { Text(kabupaten) },
                                onClick = {
                                    selectedKabupaten = kabupaten
                                    kabupatenExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hargaKost,
                        onValueChange = { if(it.all { char -> char.isDigit() }) hargaKost = it },
                        label = { Text("Harga") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = CurrencyVisualTransformation()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = periodeExpanded,
                        onExpandedChange = { periodeExpanded = !periodeExpanded },
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        OutlinedTextField(
                            value = "/ ${selectedPeriode}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodeExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = periodeExpanded,
                            onDismissRequest = { periodeExpanded = false }
                        ) {
                            periodeOptions.forEach { periode ->
                                DropdownMenuItem(
                                    text = { Text(periode) },
                                    onClick = {
                                        selectedPeriode = periode
                                        periodeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = alamatKost, onValueChange = { alamatKost = it }, label = { Text("Alamat Lengkap") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = teleponKost, onValueChange = { teleponKost = it }, label = { Text("No. Telepon") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = deskripsiKost, onValueChange = { deskripsiKost = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Gambar Kost", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = imageUri ?: existingImageUrl
                    if (displayImage != null) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = "Preview Gambar Kost",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Tidak ada gambar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Ganti Gambar")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPromo, onCheckedChange = { isPromo = it })
                    Text("Tandai sebagai Promo Spesial")
                }

                if (isPromo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hargaPromo,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                hargaPromo = newValue
                            }
                        },
                        label = { Text("Harga Promo") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = CurrencyVisualTransformation()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val processUpdate = { imageUrl: String ->
                            val tags = if (isPromo) listOf("Promo") else emptyList()
                            val priceWithPeriod = "${CurrencyVisualTransformation().filter(AnnotatedString(hargaKost)).text} / ${selectedPeriode}"
                            // Perubahan: Logika untuk harga promo
                            val promoPriceValue = if (isPromo && hargaPromo.isNotBlank()) {
                                "${CurrencyVisualTransformation().filter(AnnotatedString(hargaPromo)).text} / $selectedPeriode"
                            } else {
                                null
                            }

                            val updatedKost = Kost(
                                id = kostId,
                                name = namaKost,
                                location = selectedKabupaten,
                                price = priceWithPeriod,
                                promoPrice = promoPriceValue, // Perubahan
                                description = deskripsiKost,
                                address = alamatKost,
                                phone = teleponKost,
                                isAvailable = selectedStatus == "Tersedia",
                                imageUrl = imageUrl,
                                type = selectedType,
                                tags = tags,
                                createdAt = selectedKost?.createdAt,
                                ratings = selectedKost?.ratings ?: emptyList(),
                                bookedBy = selectedKost?.bookedBy ?: emptyList()
                            )

                            kostViewModel.updateKost(kostId, updatedKost) { success, error ->
                                if (success) {
                                    Toast.makeText(context, "Kost berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Gagal memperbarui: $error", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        if (imageUri != null) {
                            kostViewModel.uploadImageToCloudinary(imageUri!!) { result ->
                                result.onSuccess { newImageUrl ->
                                    processUpdate(newImageUrl)
                                }
                                result.onFailure {
                                    Toast.makeText(context, "Gagal mengunggah gambar baru: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            processUpdate(existingImageUrl ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isLoading
                ) {
                    Text("Simpan Perubahan")
                }
            }
        }
    }
}