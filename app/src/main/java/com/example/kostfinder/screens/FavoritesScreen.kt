package com.example.kostfinder.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.models.Kost
import com.example.kostfinder.screens.common.KostCardItem
import com.example.kostfinder.screens.common.ShimmerKostCardPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onKostClick: (Kost) -> Unit,
    userViewModel: UserViewModel,
    kostViewModel: KostViewModel = viewModel()
) {
    val userData by userViewModel.userData.collectAsState()
    val allKosts by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()

    val favoriteKostIds = userData?.favoriteKostIds ?: emptyList()
    val favoriteKosts = allKosts.filter { it.id in favoriteKostIds }

    // ## PERBAIKAN: Ambil data dari userData ##
    val recentlyViewedIds = userData?.recentlyViewedIds ?: emptyList()
    val recentlyViewedKosts = recentlyViewedIds.mapNotNull { id ->
        allKosts.find { it.id == id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorit & Aktivitas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                item { SectionTitle(title = "Favorit Saya") }
                items(3) { ShimmerKostCardPlaceholder() }
                item { SectionTitle(title = "Terakhir Dilihat") }
                items(3) { ShimmerKostCardPlaceholder() }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionTitle(title = "Favorit Saya")
                }
                if (favoriteKosts.isEmpty()) {
                    item {
                        EmptyState(message = "Anda belum memiliki kost favorit.")
                    }
                } else {
                    items(favoriteKosts) { kost ->
                        KostCardItem(kost = kost, onClick = { onKostClick(kost) })
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                }

                item {
                    SectionTitle(title = "Terakhir Dilihat")
                }
                if (recentlyViewedKosts.isEmpty()) {
                    item {
                        EmptyState(message = "Belum ada kost yang Anda lihat.")
                    }
                } else {
                    items(recentlyViewedKosts) { kost ->
                        KostCardItem(kost = kost, onClick = { onKostClick(kost) })
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray)
    }
}