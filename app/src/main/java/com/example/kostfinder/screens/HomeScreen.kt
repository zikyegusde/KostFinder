package com.example.kostfinder.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.screens.common.KostCardItem
import kotlinx.coroutines.delay

// Data class untuk item navigasi agar lebih rapi
data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, kostViewModel: KostViewModel = viewModel()) {
    // NavController ini khusus untuk navigasi di dalam Bottom Bar
    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KostFinder") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val items = listOf(
                BottomNavItem("Home", "home_content", Icons.Default.Home),
                BottomNavItem("Search", "search", Icons.Default.Search),
                BottomNavItem("Favorites", "favorites", Icons.Default.Favorite),
                BottomNavItem("Profile", "profile", Icons.Default.Person)
            )

            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        containerColor = Color(0xFFF0F4F7)
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_content",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_content") {
                HomeScreenContent(
                    mainNavController = navController,
                    kostViewModel = kostViewModel
                )
            }
            composable("search") {
                SearchScreen(navController, kostViewModel)
            }
            composable("favorites") {
                FavoritesScreen(onKostClick = { kost ->
                    navController.navigate("detail/${kost.id}")
                })
            }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    onLogoutClick = { /* Logic is inside ProfileScreen */ },
                    onEditProfileClick = { navController.navigate("editProfile") }
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    mainNavController: NavController,
    kostViewModel: KostViewModel
) {
    val kostList by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }

    val categories = listOf("Semua", "Denpasar", "Tabanan", "Jimbaran", "Putra", "Putri", "Campur")

    val filteredList = remember(selectedCategory, kostList) {
        when {
            selectedCategory == "Semua" -> kostList
            selectedCategory in listOf("Putra", "Putri", "Campur") -> {
                kostList.filter { it.type.equals(selectedCategory, ignoreCase = true) }
            }
            else -> {
                kostList.filter { it.location.contains(selectedCategory, ignoreCase = true) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            val imageUrls = listOf(
                "https://tse3.mm.bing.net/th/id/OIP.L4QxNrmQhPGgWnsTJdbCoQAAAA?pid=Api&P=0&h=180",
                "https://tse3.mm.bing.net/th/id/OIP.LCrvvcBSz2cfxkN4O31x8gHaDt?pid=Api&P=0&h=180"
            )
            AutoSlidingCarousel(imageUrls = imageUrls)
        }

        item {
            CategoryChips(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
        }
        item {
            Text(
                text = "Rekomendasi Untukmu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        if (isLoading && filteredList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(filteredList) { kost ->
                KostCardItem(kost = kost, onClick = {
                    mainNavController.navigate("detail/${kost.id}")
                })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoSlidingCarousel(imageUrls: List<String>) {
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    LaunchedEffect(Unit) {
        while(true) {
            delay(3000) // Jeda 3 detik
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrls[page]),
                    contentDescription = "Iklan ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                ),
                leadingIcon = if (isSelected) {
                    { Icon(imageVector = Icons.Default.Done, contentDescription = "Done") }
                } else {
                    null
                }
            )
        }
    }
}
