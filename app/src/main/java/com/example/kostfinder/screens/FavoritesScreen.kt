package com.example.kostfinder.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kostfinder.data.FavoritesManager
import com.example.kostfinder.models.Kost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onKostClick: (Kost) -> Unit) {
    val favorites by remember { mutableStateOf(FavoritesManager.getFavorites()) }

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
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada kost favorit")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(favorites) { kost ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { onKostClick(kost) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(kost.name, style = MaterialTheme.typography.titleMedium)
                            Text(kost.location, style = MaterialTheme.typography.bodyMedium)
                            Text("Rp${kost.price}/bulan", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
