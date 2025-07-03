package com.example.kostfinder.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.models.Kost
import com.example.kostfinder.data.FavoritesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(kost: Kost, navController: NavHostController) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(FavoritesManager.isFavorite(kost)) }

    var newRating by remember { mutableIntStateOf(0) }
    var newComment by remember { mutableStateOf("") }

    var isBooked by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }

    val averageRating = if (kost.ratings.isNotEmpty()) {
        kost.ratings.map { it.first }.average()
    } else 0.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(kost.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isFavorite) {
                            FavoritesManager.removeFavorite(kost)
                        } else {
                            FavoritesManager.addFavorite(kost)
                        }
                        isFavorite = !isFavorite
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Unfavorite" else "Favorite"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = rememberAsyncImagePainter(kost.imageUrl),
                contentDescription = kost.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(kost.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(kost.location, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Harga: ${kost.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (kost.isAvailable) "Status: Tersedia" else "Status: Penuh",
                color = if (kost.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Deskripsi:", style = MaterialTheme.typography.titleMedium)
            Text(kost.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Alamat:", style = MaterialTheme.typography.titleMedium)
            Text(kost.address, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Nomor Telepon:", style = MaterialTheme.typography.titleMedium)
            Text(kost.phone, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${kost.phone}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Telepon Sekarang")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showBookingDialog = true },
                enabled = kost.isAvailable && !isBooked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isBooked) "Sudah Dipesan" else "Booking Sekarang")
            }

            if (showBookingDialog) {
                AlertDialog(
                    onDismissRequest = { showBookingDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            isBooked = true
                            showBookingDialog = false
                        }) {
                            Text("Konfirmasi")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBookingDialog = false }) {
                            Text("Batal")
                        }
                    },
                    title = { Text("Konfirmasi Booking") },
                    text = { Text("Apakah Anda yakin ingin mem-booking kost ini?") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("Rating: ${"%.1f".format(averageRating)} / 5", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Ulasan:", style = MaterialTheme.typography.titleMedium)
            kost.ratings.forEach { (rating, comment) ->
                Text("⭐ $rating - $comment", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Beri Ulasan:", style = MaterialTheme.typography.titleMedium)

            Row {
                (1..5).forEach { star ->
                    IconButton(onClick = { newRating = star }) {
                        Icon(
                            imageVector = if (newRating >= star) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Rating"
                        )
                    }
                }
            }

            OutlinedTextField(
                value = newComment,
                onValueChange = { newComment = it },
                label = { Text("Komentar") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (newRating > 0 && newComment.isNotBlank()) {
                        kost.ratings.add(Pair(newRating, newComment))
                        newRating = 0
                        newComment = ""
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                Text("Kirim Ulasan")
            }
        }
    }
}
