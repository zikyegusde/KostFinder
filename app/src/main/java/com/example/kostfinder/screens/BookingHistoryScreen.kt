package com.example.kostfinder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.models.Booking
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val userData by userViewModel.userData.collectAsState()
    val bookingHistory = userData?.bookings?.sortedByDescending { it.bookingDate } ?: emptyList()

    // State untuk mengontrol dialog konfirmasi
    var showCancelDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Booking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (bookingHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Anda belum pernah melakukan booking.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookingHistory) { booking ->
                    BookingHistoryItem(
                        booking = booking,
                        onCancelClick = {
                            bookingToCancel = it
                            showCancelDialog = true
                        }
                    )
                }
            }
        }

        // Dialog konfirmasi pembatalan
        if (showCancelDialog && bookingToCancel != null) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Konfirmasi Pembatalan") },
                text = { Text("Apakah Anda yakin ingin membatalkan booking untuk ${bookingToCancel?.kostName}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            bookingToCancel?.let { booking ->
                                userViewModel.cancelBooking(booking, context) { success ->
                                    if (success) {
                                        showCancelDialog = false
                                        bookingToCancel = null
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Ya, Batalkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Tidak")
                    }
                }
            )
        }
    }
}

@Composable
fun BookingHistoryItem(
    booking: Booking,
    onCancelClick: (Booking) -> Unit // Tambahkan parameter ini
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = booking.kostImageUrl,
                    contentDescription = booking.kostName,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.kostName, fontWeight = FontWeight.Bold)
                    Text(booking.kostPrice, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    val date = booking.bookingDate?.let {
                        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(it)
                    } ?: "Tanggal tidak tersedia"
                    Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Tombol untuk membatalkan booking
            OutlinedButton(
                onClick = { onCancelClick(booking) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batalkan Booking", fontSize = 12.sp)
            }
        }
    }
}