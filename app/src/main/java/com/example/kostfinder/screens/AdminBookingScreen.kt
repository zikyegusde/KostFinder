package com.example.kostfinder.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Make sure this is imported
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.models.Booking
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingScreen(
    navController: NavController,
    kostId: String,
    kostViewModel: KostViewModel = viewModel()
) {
    LaunchedEffect(kostId) {
        kostViewModel.getBookingsForKost(kostId)
        kostViewModel.getKostById(kostId) // Also fetch kost details if needed for the title
    }

    val bookings by kostViewModel.kostBookings.collectAsState()
    val kost by kostViewModel.selectedKost.collectAsState() // Fetching kost details
    val context = LocalContext.current

    var showRejectDialog by remember { mutableStateOf(false) }
    var showApproveDialog by remember { mutableStateOf(false) }
    var bookingToManage by remember { mutableStateOf<Booking?>(null) }
    var rejectionMessage by remember { mutableStateOf("") }
    // Initialize paymentDetails within the composable scope, potentially using bookingToManage
    var paymentDetails by remember(bookingToManage) {
        mutableStateOf(
            if (bookingToManage != null) "BCA 123456789 a/n Pemilik Kos\nTotal: ${bookingToManage?.kostPrice}" else "BCA 123456789 a/n Pemilik Kos\nTotal: "
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking: ${kost?.name ?: "..."}") }, // Display kost name
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada booking untuk kos ini.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings) { booking -> // <-- This 'booking' is the item for the current iteration
                    BookingManageItem(
                        booking = booking,
                        onApproveClick = { currentBooking -> // <-- Give the parameter a name (e.g., currentBooking)
                            bookingToManage = currentBooking
                            // Recalculate payment details based on the selected booking
                            paymentDetails = "BCA 123456789 a/n Pemilik Kos\nTotal: ${currentBooking.kostPrice}"
                            showApproveDialog = true
                        },
                        onRejectClick = { currentBooking -> // <-- Give the parameter a name here too
                            bookingToManage = currentBooking
                            rejectionMessage = "" // Reset message
                            showRejectDialog = true
                        }
                    )
                }
            }
        }

        // Dialog untuk Menolak Booking
        if (showRejectDialog && bookingToManage != null) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Tolak Booking") },
                text = {
                    Column {
                        Text("Masukkan alasan penolakan untuk ${bookingToManage?.userName}:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionMessage,
                            onValueChange = { rejectionMessage = it },
                            label = { Text("Alasan penolakan") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rejectionMessage.isBlank()) {
                                Toast.makeText(context, "Alasan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            kostViewModel.updateBookingStatus(
                                bookingId = bookingToManage!!.id,
                                status = "Rejected",
                                rejectionMessage = rejectionMessage
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Booking ditolak", Toast.LENGTH_SHORT).show()
                                    showRejectDialog = false
                                } else {
                                    Toast.makeText(context, "Gagal menolak booking", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Tolak") }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) { Text("Batal") }
                }
            )
        }

        // Dialog untuk Menyetujui Booking
        if (showApproveDialog && bookingToManage != null) {
            AlertDialog(
                onDismissRequest = { showApproveDialog = false },
                title = { Text("Setujui Booking") },
                text = {
                    Column {
                        Text("Masukkan detail pembayaran untuk ${bookingToManage?.userName}:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = paymentDetails,
                            onValueChange = { paymentDetails = it },
                            label = { Text("Detail Rekening & Nominal") },
                            modifier = Modifier.height(150.dp) // Adjusted height for better visibility
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (paymentDetails.isBlank()) {
                                Toast.makeText(context, "Detail pembayaran tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            kostViewModel.updateBookingStatus(
                                bookingId = bookingToManage!!.id,
                                status = "Approved",
                                paymentDetails = paymentDetails
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Booking disetujui", Toast.LENGTH_SHORT).show()
                                    showApproveDialog = false
                                } else {
                                    Toast.makeText(context, "Gagal menyetujui booking", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) { Text("Setujui") }
                },
                dismissButton = {
                    TextButton(onClick = { showApproveDialog = false }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun BookingManageItem(
    booking: Booking,
    onApproveClick: (Booking) -> Unit, // Expect a Booking parameter
    onRejectClick: (Booking) -> Unit   // Expect a Booking parameter
) {
    val date = booking.bookingDate?.let {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(it)
    } ?: "Tanggal tidak tersedia"

    val statusColor = when (booking.status) {
        "Approved" -> Color(0xFF388E3C) // Dark Green
        "Rejected" -> Color(0xFFD32F2F) // Dark Red
        else -> MaterialTheme.colorScheme.onSurface // Default text color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) { // Add padding to prevent text overlap
                    Text(booking.userName, fontWeight = FontWeight.Bold)
                    Text(booking.userEmail, style = MaterialTheme.typography.bodySmall)
                    Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(
                    booking.status,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Only show buttons if status is Pending
            if (booking.status == "Pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onApproveClick(booking) }, // Pass the booking object
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)) // Dark Green
                    ) { Text("Setujui") }

                    Button(
                        onClick = { onRejectClick(booking) }, // Pass the booking object
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Tolak") }
                }
            }
            // Optionally display rejection message or payment details if needed
            else if (booking.status == "Rejected" && booking.rejectionMessage != null) {
                Text("Alasan: ${booking.rejectionMessage}", style = MaterialTheme.typography.bodySmall, color = statusColor)
            } else if (booking.status == "Approved" && booking.adminPaymentDetails != null) {
                Text("Info Pembayaran Dikirim", style = MaterialTheme.typography.bodySmall, color = statusColor)
            }
        }
    }
}