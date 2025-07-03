package com.example.kostfinder.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.KostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(kostId: String, navController: NavHostController, kostViewModel: KostViewModel = viewModel()) {
    val context = LocalContext.current

    LaunchedEffect(kostId) {
        kostViewModel.getKostById(kostId)
    }

    val kost by kostViewModel.selectedKost.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(kost?.name ?: "Detail Kost") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                kost?.let { currentKost ->
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(currentKost.imageUrl),
                            contentDescription = currentKost.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(currentKost.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        // ... (Tampilkan semua detail kost lainnya dari `currentKost`)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${currentKost.phone}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Telepon Sekarang")
                        }
                    }
                }
            }
        }
    }
}