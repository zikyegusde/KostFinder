package com.example.kostfinder.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.models.Kost
import com.example.kostfinder.screens.common.KostCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onKostClick: (Kost) -> Unit,
    userViewModel: UserViewModel = viewModel(),
    kostViewModel: KostViewModel = viewModel()
) {
    val userData by userViewModel.userData.collectAsState()
    val allKosts by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()

    val favoriteKostIds = userData?.favoriteKostIds ?: emptyList()
    val favoriteKosts = allKosts.filter { it.id in favoriteKostIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorit Saya") },
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
            if (isLoading && favoriteKosts.isEmpty()) {
                CircularProgressIndicator()
            } else if (favoriteKosts.isEmpty()) {
                Text("Anda belum memiliki kost favorit.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favoriteKosts) { kost ->
                        KostCardItem(kost = kost, onClick = { onKostClick(kost) })
                    }
                }
            }
        }
    }
}
