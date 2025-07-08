package com.example.kostfinder.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.models.Kost
import com.example.kostfinder.screens.common.KostCardItem
import com.example.kostfinder.screens.common.ShimmerKostCardPlaceholder
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun FavoritesScreen(
    onKostClick: (Kost) -> Unit,
    userViewModel: UserViewModel,
    kostViewModel: KostViewModel = viewModel()
) {
    val tabs = listOf("Favorit Saya", "Terakhir Dilihat")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

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
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> FavoriteList(onKostClick, userViewModel, kostViewModel)
                    1 -> RecentlyViewedList(onKostClick, userViewModel, kostViewModel)
                }
            }
        }
    }
}

@Composable
fun FavoriteList(
    onKostClick: (Kost) -> Unit,
    userViewModel: UserViewModel,
    kostViewModel: KostViewModel
) {
    val userData by userViewModel.userData.collectAsState()
    val allKosts by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()

    val favoriteKostIds = userData?.favoriteKostIds ?: emptyList()
    val favoriteKosts = allKosts.filter { it.id in favoriteKostIds }

    if (isLoading && favoriteKosts.isEmpty()) {
        LoadingState()
    } else if (favoriteKosts.isEmpty()) {
        EmptyState(message = "Anda belum memiliki kost favorit.")
    } else {
        KostList(kosts = favoriteKosts, onKostClick = onKostClick)
    }
}

@Composable
fun RecentlyViewedList(
    onKostClick: (Kost) -> Unit,
    userViewModel: UserViewModel,
    kostViewModel: KostViewModel
) {
    val userData by userViewModel.userData.collectAsState()
    val allKosts by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()

    val recentlyViewedIds = userData?.recentlyViewedIds ?: emptyList()
    val recentlyViewedKosts = recentlyViewedIds.mapNotNull { id ->
        allKosts.find { it.id == id }
    }

    if (isLoading && recentlyViewedKosts.isEmpty()) {
        LoadingState()
    } else if (recentlyViewedKosts.isEmpty()) {
        EmptyState(message = "Belum ada kost yang Anda lihat.")
    } else {
        KostList(kosts = recentlyViewedKosts, onKostClick = onKostClick)
    }
}

@Composable
fun KostList(kosts: List<Kost>, onKostClick: (Kost) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(kosts) { kost ->
            KostCardItem(kost = kost, onClick = { onKostClick(kost) })
        }
    }
}

@Composable
fun LoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(5) { ShimmerKostCardPlaceholder() }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray)
    }
}