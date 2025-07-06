package com.example.kostfinder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.screens.common.KostCardItem
import com.example.kostfinder.screens.common.ShimmerKostCardPlaceholder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(navController: NavController, kostViewModel: KostViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val allKosts by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()

    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedKabupaten by remember { mutableStateOf<String?>(null) }
    var isKabupatenMenuExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Putra", "Putri", "Campur", "Kos Murah", "Kabupaten")
    val kabupatenOptions = listOf("Badung", "Bangli", "Buleleng", "Denpasar", "Gianyar", "Jembrana", "Karangasem", "Klungkung", "Tabanan")

    fun parsePrice(price: String): Long {
        try {
            val lowerCasePrice = price.lowercase(Locale.getDefault()).replace("rp", "").trim()
            if (lowerCasePrice.contains("juta") || lowerCasePrice.contains("jt")) {
                val numberPart = lowerCasePrice.split("juta")[0].split("jt")[0].replace(Regex("[^\\d,.]"), "").replace(",", ".")
                return ((numberPart.toDoubleOrNull() ?: 0.0) * 1_000_000).toLong()
            }
            if (lowerCasePrice.contains("ribu") || lowerCasePrice.contains("rb") || lowerCasePrice.endsWith("k")) {
                val numberPart = lowerCasePrice.split("ribu")[0].split("rb")[0].split("k")[0].replace(Regex("[^\\d,.]"), "").replace(",", ".")
                return ((numberPart.toDoubleOrNull() ?: 0.0) * 1_000).toLong()
            }
            val cleanedString = lowerCasePrice.replace(Regex("\\D"), "")
            if (cleanedString.isBlank()) return 0L
            return cleanedString.toLong()
        } catch (_: Exception) {
            return 0L
        }
    }

    val filteredKosts = remember(searchQuery, selectedCategory, selectedKabupaten, allKosts) {
        var result = allKosts

        // 1. Filter berdasarkan teks pencarian
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase(Locale.getDefault())
            result = result.filter {
                it.name.lowercase(Locale.getDefault()).contains(query) ||
                        it.location.lowercase(Locale.getDefault()).contains(query) ||
                        it.address.lowercase(Locale.getDefault()).contains(query) ||
                        it.type.lowercase(Locale.getDefault()).contains(query)
            }
        }

        // 2. Filter berdasarkan kategori chip
        result = when (selectedCategory) {
            "Kos Murah" -> result.filter { kost ->
                // ## PERUBAHAN LOGIKA HARGA MURAH ##
                // Cek harga promo dulu, jika tidak ada, baru cek harga asli.
                val priceToCheck = kost.promoPrice ?: kost.price
                val priceValue = parsePrice(priceToCheck)
                priceValue > 0 && priceValue <= 1_000_000
                // ## AKHIR PERUBAHAN ##
            }
            "Putra", "Putri", "Campur" -> result.filter { it.type.equals(selectedCategory, ignoreCase = true) }
            "Kabupaten" -> {
                if (selectedKabupaten != null) {
                    result.filter { it.location.equals(selectedKabupaten, ignoreCase = true) }
                } else {
                    result
                }
            }
            else -> result
        }
        result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Cari Kost Impianmu", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Masukkan nama, lokasi, atau tipe...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { category ->
                if (category == "Kabupaten") {
                    Box {
                        FilterChip(
                            selected = selectedCategory == "Kabupaten",
                            onClick = {
                                selectedCategory = "Kabupaten"
                                isKabupatenMenuExpanded = true
                            },
                            label = { Text(selectedKabupaten ?: "Kabupaten") },
                            leadingIcon = if (selectedCategory == "Kabupaten" && selectedKabupaten != null) {
                                { Icon(imageVector = Icons.Default.Done, contentDescription = "Done") }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        DropdownMenu(
                            expanded = isKabupatenMenuExpanded,
                            onDismissRequest = { isKabupatenMenuExpanded = false }
                        ) {
                            kabupatenOptions.forEach { kabupaten ->
                                DropdownMenuItem(
                                    text = { Text(kabupaten) },
                                    onClick = {
                                        selectedKabupaten = kabupaten
                                        isKabupatenMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            selectedKabupaten = null
                        },
                        label = { Text(category) },
                        leadingIcon = if (selectedCategory == category) {
                            { Icon(imageVector = Icons.Default.Done, contentDescription = "Done") }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Hasil Pencarian (${filteredKosts.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(5) { ShimmerKostCardPlaceholder() }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredKosts) { kost ->
                    KostCardItem(kost = kost, onClick = {
                        navController.navigate("detail/${kost.id}")
                    })
                }
            }
        }
    }
}