package com.example.kostfinder.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    kostId: String,
    navController: NavHostController,
    kostViewModel: KostViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser

    // LaunchedEffect akan memanggil getKostById setiap kali kostId berubah
    LaunchedEffect(kostId) {
        if (kostId.isNotBlank()) {
            kostViewModel.getKostById(kostId)
        }
    }

    val kost by kostViewModel.selectedKost.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()
    val userData by userViewModel.userData.collectAsState()

    val isFavorite = userData?.favoriteKostIds?.contains(kostId) == true

    var showBookingDialog by remember { mutableStateOf(false) }
    var newRating by remember { mutableFloatStateOf(0f) }
    var newComment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(kost?.name ?: "Detail Kost") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Tombol Favorit
                    IconButton(onClick = { userViewModel.toggleFavorite(kostId, context) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (kost == null) {
                Text("Gagal memuat detail kost. Silakan coba lagi.")
            } else {
                kost?.let { currentKost ->
                    val isBookedByCurrentUser = currentUser?.uid in currentKost.bookedBy
                    val averageRating = if (currentKost.ratings.isNotEmpty()) {
                        currentKost.ratings.mapNotNull { it["rating"] as? Double }.average()
                    } else 0.0

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(currentKost.imageUrl),
                                contentDescription = currentKost.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(currentKost.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(currentKost.location, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(currentKost.price, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Menampilkan semua detail
                        DetailSection("Deskripsi", currentKost.description)
                        DetailSection("Alamat Lengkap", currentKost.address)
                        DetailSection("Nomor Telepon", currentKost.phone)

                        Text(
                            text = if (currentKost.isAvailable) "Status: Tersedia" else "Status: Penuh",
                            color = if (currentKost.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${currentKost.phone}") }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text("Telepon")
                            }
                            Button(
                                onClick = { showBookingDialog = true },
                                enabled = currentKost.isAvailable && !isBookedByCurrentUser,
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text(if (isBookedByCurrentUser) "Sudah Dipesan" else "Booking")
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Rating Section
                        Text("Rating & Ulasan (${String.format(Locale.US, "%.1f", averageRating)}/5.0)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentKost.ratings.isEmpty()) {
                            Text("Belum ada ulasan.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        } else {
                            currentKost.ratings.forEach { ratingMap ->
                                val ratingValue = (ratingMap["rating"] as? Double)?.toFloat() ?: 0f
                                val comment = ratingMap["comment"] as? String ?: ""
                                val userName = ratingMap["userName"] as? String ?: "Anonymous"
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("$userName - $ratingValue ★", fontWeight = FontWeight.Bold)
                                    Text("\"$comment\"")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Rating
                        Text("Beri Ulasan Anda", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        RatingBar(rating = newRating, onRatingChanged = { newRating = it })
                        OutlinedTextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            label = { Text("Tulis komentar Anda...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newRating > 0 && newComment.isNotBlank() && currentUser != null) {
                                    val ratingData = mapOf(
                                        "userId" to currentUser.uid,
                                        "userName" to (currentUser.displayName ?: currentUser.email ?: "Anonymous"),
                                        "rating" to newRating,
                                        "comment" to newComment
                                    )
                                    kostViewModel.addRating(currentKost.id, ratingData) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Ulasan terkirim!", Toast.LENGTH_SHORT).show()
                                            newRating = 0f
                                            newComment = ""
                                        } else {
                                            Toast.makeText(context, "Gagal mengirim ulasan.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Harap berikan rating dan komentar.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kirim Ulasan")
                        }
                    }
                }
            }
        }

        if (showBookingDialog) {
            AlertDialog(
                onDismissRequest = { showBookingDialog = false },
                title = { Text("Konfirmasi Booking") },
                text = { Text("Apakah Anda yakin ingin memesan kost ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        currentUser?.uid?.let { userId ->
                            kost?.id?.let { kostId ->
                                kostViewModel.bookKost(kostId, userId) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Booking berhasil!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Gagal melakukan booking.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        showBookingDialog = false
                    }) {
                        Text("Konfirmasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBookingDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    if (content.isNotBlank()) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(modifier = modifier) {
        (1..5).forEach { starIndex ->
            val isSelected = starIndex <= rating
            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(starIndex.toFloat()) }
            )
        }
    }
}
