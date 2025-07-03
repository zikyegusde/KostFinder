package com.example.kostfinder.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.kostfinder.models.Kost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onKostClick: (Kost) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorit") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Halaman Favorit belum diimplementasikan.")
        }
    }
}
