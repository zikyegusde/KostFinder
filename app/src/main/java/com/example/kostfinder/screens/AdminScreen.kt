package com.example.kostfinder.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.models.Kost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat
import java.util.Locale

// Kelas CurrencyVisualTransformation tidak berubah
class CurrencyVisualTransformation(
    private val locale: Locale = Locale("id", "ID")
) : VisualTransformation {
    private val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        maximumFractionDigits = 0
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.trim()
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val number = originalText.toLongOrNull() ?: 0L
        val formattedText = numberFormat.format(number)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val formattedLength = formattedText.length
                val originalLength = originalText.length
                val separators = formattedLength - originalLength
                return offset + separators
            }

            override fun transformedToOriginal(offset: Int): Int {
                return originalText.length
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController, kostViewModel: KostViewModel = viewModel()) {
    var namaKost by remember { mutableStateOf("") }
    var deskripsiKost by remember { mutableStateOf("") }
    var hargaKost by remember { mutableStateOf("") }
    var alamatKost by remember { mutableStateOf("") }
    var teleponKost by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var periodeExpanded by remember { mutableStateOf(false) }
    var selectedPeriode by remember { mutableStateOf("Bulan") }
    val periodeOptions = listOf("Bulan", "Tahun")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("Tersedia") }
    val statusOptions = listOf("Tersedia", "Penuh")

    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("Campur") }
    val kostTypes = listOf("Campur", "Putra", "Putri")

    var kabupatenExpanded by remember { mutableStateOf(false) }
    var selectedKabupaten by remember { mutableStateOf("Denpasar") }
    val kabupatenOptions = listOf("Badung", "Bangli", "Buleleng", "Denpasar", "Gianyar", "Jembrana", "Karangasem", "Klungkung", "Tabanan")

    var isPromo by remember { mutableStateOf(false) }

    val kostList by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Text("Panel Admin", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tambah Kost Baru", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

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
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                hargaKost = newValue
                            }
                        },
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

                Column {
                    Text("Gambar Kost", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUri),
                                contentDescription = "Preview Gambar Kost",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("Belum ada gambar dipilih")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pilih Gambar")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {}, readOnly = true,
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
                            onValueChange = {}, readOnly = true,
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPromo, onCheckedChange = { isPromo = it })
                    Text("Tandai sebagai Promo Spesial")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (imageUri == null) {
                            Toast.makeText(context, "Harap pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        imageUri?.let { uri ->
                            kostViewModel.uploadImageToCloudinary(uri) { result ->
                                result.onSuccess { downloadUrl ->
                                    val tags = if (isPromo) listOf("Promo") else emptyList()
                                    val priceWithPeriod = "${CurrencyVisualTransformation().filter(AnnotatedString(hargaKost)).text} / $selectedPeriode"
                                    val newKost = Kost(
                                        name = namaKost,
                                        location = selectedKabupaten,
                                        price = priceWithPeriod,
                                        description = deskripsiKost,
                                        address = alamatKost,
                                        phone = teleponKost,
                                        isAvailable = selectedStatus == "Tersedia",
                                        imageUrl = downloadUrl,
                                        type = selectedType,
                                        tags = tags
                                    )
                                    kostViewModel.addKost(newKost) { success, error ->
                                        if (success) {
                                            Toast.makeText(context, "Kost berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                            namaKost = ""; hargaKost = ""; alamatKost = ""; teleponKost = ""; deskripsiKost = ""; imageUri = null; isPromo = false
                                        } else {
                                            Toast.makeText(context, "Gagal menyimpan data: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                result.onFailure {
                                    Toast.makeText(context, "Gagal mengunggah gambar: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isLoading
                ) {
                    Text("Simpan Kost")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                Text("Daftar Kost Saat Ini", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading && kostList.isEmpty()) {
                item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
            } else {
                items(kostList) { kost ->
                    // --- PERBAIKAN: Menambahkan `clickable` pada Card ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { navController.navigate("detail/${kost.id}") },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        // ----------------------------------------------------
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(kost.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(kost.location, style = MaterialTheme.typography.bodyMedium)
                                Text(kost.price, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(onClick = { navController.navigate("editKost/${kost.id}") }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Kost")
                            }

                            IconButton(
                                onClick = {
                                    kostViewModel.deleteKost(kost.id) { success, error ->
                                        if (success) Toast.makeText(context, "Kost dihapus", Toast.LENGTH_SHORT).show()
                                        else Toast.makeText(context, "Gagal menghapus: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Kost", tint = MaterialTheme.colorScheme.error)
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
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
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