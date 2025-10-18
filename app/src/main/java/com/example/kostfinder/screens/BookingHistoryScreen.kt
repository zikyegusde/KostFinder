package com.example.kostfinder.screens

import androidx.compose.foundation.background
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
    // --- MODIFIKASI: Ambil booking dari flow baru ---
    val bookingHistory by userViewModel.bookingHistory.collectAsState()
    // ----------------------------------------------

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
    onCancelClick: (Booking) -> Unit
) {
    // Tentukan warna berdasarkan status
    val statusColor = when (booking.status) {
        "Approved" -> Color(0xFFDCEDC8) // Hijau muda
        "Rejected" -> Color(0xFFFFCDD2) // Merah muda
        else -> MaterialTheme.colorScheme.surfaceVariant // Default
    }

    val statusTextColor = when (booking.status) {
        "Approved" -> Color(0xFF388E3C) // Hijau tua
        "Rejected" -> Color(0xFFD32F2F) // Merah tua
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor) // Terapkan warna
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
                // Tampilkan Status
                Text(
                    booking.status,
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Tampilkan info tambahan berdasarkan status
            when (booking.status) {
                "Approved" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text("Silakan lakukan pembayaran ke:", fontWeight = FontWeight.Bold)
                        Text(booking.adminPaymentDetails ?: "Admin belum memasukkan detail rekening.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sebesar: ${booking.kostPrice}", fontWeight = FontWeight.SemiBold)
                    }
                }
                "Rejected" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text("Alasan Penolakan:", fontWeight = FontWeight.Bold)
                        Text(booking.rejectionMessage ?: "Admin tidak memberikan alasan.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                "Pending" -> {
                    // Tampilkan tombol batal HANYA jika masih pending
                    OutlinedButton(
                        onClick = { onCancelClick(booking) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batalkan Booking", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}