package com.example.kostfinder.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.screens.common.KostCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullKostListScreen(
    navController: NavController,
    listType: String?,
    kostViewModel: KostViewModel = viewModel()
) {
    val title = when (listType) {
        "promo" -> "Promo Spesial"
        "popular" -> "Kost Populer"
        "new" -> "Baru Ditambahkan"
        else -> "Daftar Kost"
    }

    val kostsToShow by remember(listType) {
        when (listType) {
            "promo" -> kostViewModel.promoKosts
            "popular" -> kostViewModel.popularKosts
            "new" -> kostViewModel.newKosts
            else -> kostViewModel.kostList
        }
    }.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(kostsToShow) { kost ->
                KostCardItem(kost = kost, onClick = {
                    navController.navigate("detail/${kost.id}")
                })
            }
        }
    }
}