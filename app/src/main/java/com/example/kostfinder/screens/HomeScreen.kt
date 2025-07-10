package com.example.kostfinder.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.R
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.models.Kost
import com.example.kostfinder.screens.common.KostCardItem
import com.example.kostfinder.screens.common.ShimmerKostCardPlaceholder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.util.Locale

// Data class untuk mempermudah pengelolaan kategori visual
data class VisualCategory(val name: String, val icon: ImageVector)

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    kostViewModel: KostViewModel = viewModel(),
    userViewModel: UserViewModel
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val items = listOf(
                BottomNavItem("Home", "home_content", Icons.Filled.Home, Icons.Outlined.Home),
                BottomNavItem("Search", "search", Icons.Filled.Search, Icons.Outlined.Search),
                BottomNavItem("Favorites", "favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
                BottomNavItem("Profile", "profile", Icons.Filled.Person, Icons.Outlined.Person)
            )
            NavigationBar {
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
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
                SearchScreen(navController = navController, kostViewModel = kostViewModel)
            }
            composable("favorites") {
                FavoritesScreen(
                    onKostClick = { kost -> navController.navigate("detail/${kost.id}") },
                    userViewModel = userViewModel
                )
            }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    onLogoutClick = {
                        Firebase.auth.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
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
    val allKosts by kostViewModel.kostList.collectAsState()
    val promoKosts by kostViewModel.promoKosts.collectAsState()
    val popularKosts by kostViewModel.popularKosts.collectAsState()
    val newKosts by kostViewModel.newKosts.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedKabupaten by remember { mutableStateOf<String?>(null) }
    val kabupatenOptions = listOf("Badung", "Bangli", "Buleleng", "Denpasar", "Gianyar", "Jembrana", "Karangasem", "Klungkung", "Tabanan")

    val filteredList = remember(selectedCategory, selectedKabupaten, allKosts) {
        when {
            selectedKabupaten != null -> {
                allKosts.filter { it.location.equals(selectedKabupaten, ignoreCase = true) }
            }
            selectedCategory == "Semua" -> allKosts
            else -> allKosts.filter { it.type.equals(selectedCategory, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            MamikosStyleHeader()
        }
        item {
            GreetingCarousel()
        }
        item {
            val imageUrls = listOf(
                "https://images.tokopedia.net/img/WMkIgA/2021/6/15/c797e9a1-4357-48ff-bd80-6fee056e33ca.png",
                "https://i.ytimg.com/vi/OznfMwKoY8Y/maxresdefault.jpg",
                "https://assets.grab.com/wp-content/uploads/sites/9/2025/02/19153558/OG20250214-01_LandingPage_1200x630_GE.jpg",
                "https://i.ytimg.com/vi/RIrARqvhe1Y/maxresdefault.jpg",
                "https://pbs.twimg.com/media/EV1a3DiUYAAdaN-.jpg",
                "https://i.ytimg.com/vi/y6N-5H2H6pc/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLBoDMy-C9qaxIy4S53NXO5wo5n1OQ",
                "https://d2z8yrol3i8wid.cloudfront.net/wp-content/uploads/2021/03/Akulaku-Finance.jpg"
            )
            AutoSlidingCarousel(imageUrls = imageUrls)
        }
        item {
            VisualCategorySection(
                selectedCategory = selectedCategory,
                onCategorySelected = { categoryName ->
                    selectedCategory = categoryName
                    selectedKabupaten = null
                },
                selectedKabupaten = selectedKabupaten,
                onKabupatenSelected = { kabupatenName ->
                    selectedKabupaten = kabupatenName
                    selectedCategory = "Kabupaten"
                },
                kabupatenOptions = kabupatenOptions
            )
        }

        if (selectedCategory == "Semua" && selectedKabupaten == null) {
            item {
                PromoNgebutSection(
                    kosts = promoKosts,
                    isLoading = isLoading,
                    onKostClick = { mainNavController.navigate("detail/${it.id}") },
                    onSeeAllClick = { mainNavController.navigate("full_kost_list/promo") }
                )
            }
            item {
                RecommendationSession(
                    title = "Kost Populer",
                    kosts = popularKosts,
                    isLoading = isLoading,
                    onKostClick = { mainNavController.navigate("detail/${it.id}") },
                    onSeeAllClick = { mainNavController.navigate("full_kost_list/popular") }
                )
            }
            item {
                RecommendationSession(
                    title = "Baru Ditambahkan",
                    kosts = newKosts,
                    isLoading = isLoading,
                    onKostClick = { mainNavController.navigate("detail/${it.id}") },
                    onSeeAllClick = { mainNavController.navigate("full_kost_list/new") }
                )
            }
        } else {
            val title = when {
                selectedKabupaten != null -> "Hasil untuk \"$selectedKabupaten\""
                else -> "Hasil untuk \"$selectedCategory\""
            }
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (isLoading) {
                items(5) { ShimmerKostCardPlaceholder() }
            } else if (filteredList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada kost ditemukan untuk filter ini.")
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GreetingCarousel() {
    val greetings = remember {
        listOf(
            "Hai, Sudah Siap Menemukan Kost Impianmu Hari Ini?",
            "Yuk Ngekos dengan Nyaman & Hemat!",
            "Temukan Kost Idamanmu Sekarang Juga, Tanpa Ribet!",
            "Selamat Datang! Cek Promo Kost Seru Hari Ini",
            "Pilih Kost Sesuai Gayamu, Bebas Pilih di Sini!"
        )
    }
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(4000)
            currentIndex = (currentIndex + 1) % greetings.size
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search Icon",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith
                            fadeOut(animationSpec = tween(500))
                }, label = "Greeting Text Animation"
            ) { targetIndex ->
                Text(
                    text = greetings[targetIndex],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun MamikosStyleHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "KostFinder",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "#EnaknyaNgekos",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cari & Sewa Kos Mudah",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
            Image(
                painter = painterResource(id = R.drawable.orang_nunjuk),
                contentDescription = "Ilustrasi",
                modifier = Modifier.size(130.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualCategorySection(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedKabupaten: String?,
    onKabupatenSelected: (String) -> Unit,
    kabupatenOptions: List<String>
) {
    val visualCategories = listOf(
        VisualCategory("Putra", Icons.Default.Male),
        VisualCategory("Putri", Icons.Default.Female),
        VisualCategory("Campur", Icons.Default.Bed)
    )

    Column {
        Text(
            text = "Kategori Pilihan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(visualCategories) { category ->
                Box(Modifier.padding(horizontal = 6.dp)) {
                    VisualCategoryCard(
                        category = category,
                        isSelected = selectedCategory == category.name,
                        onClick = { onCategorySelected(category.name) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == "Semua" && selectedKabupaten == null,
                onClick = { onCategorySelected("Semua") },
                label = { Text("Tampilkan Semua") }
            )
            KabupatenFilter(
                selectedKabupaten = selectedKabupaten,
                onKabupatenSelected = onKabupatenSelected,
                kabupatenOptions = kabupatenOptions
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KabupatenFilter(
    selectedKabupaten: String?,
    onKabupatenSelected: (String) -> Unit,
    kabupatenOptions: List<String>
) {
    var kabupatenExpanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selectedKabupaten != null,
            onClick = { kabupatenExpanded = true },
            label = { Text(selectedKabupaten ?: "Pilih Kabupaten") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
        )
        DropdownMenu(
            expanded = kabupatenExpanded,
            onDismissRequest = { kabupatenExpanded = false }
        ) {
            kabupatenOptions.forEach { kabupaten ->
                DropdownMenuItem(
                    text = { Text(kabupaten) },
                    onClick = {
                        onKabupatenSelected(kabupaten)
                        kabupatenExpanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun PromoNgebutSection(
    kosts: List<Kost>,
    isLoading: Boolean,
    onKostClick: (Kost) -> Unit,
    onSeeAllClick: () -> Unit
) {
    if (isLoading || kosts.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            PromoNgebutBackground()
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Promo Spesial",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = onSeeAllClick) {
                        Text("Lihat Semua", color = Color.White.copy(alpha = 0.8f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    reverseLayout = true,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    if (isLoading && kosts.isEmpty()) {
                        items(3) {
                            ShimmerKostCardPlaceholder(modifier = Modifier.width(160.dp))
                        }
                    } else {
                        items(kosts.reversed()) { kost ->
                            SmallPromoCard(kost = kost, onClick = { onKostClick(kost) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationSession(
    title: String,
    kosts: List<Kost>,
    isLoading: Boolean,
    onKostClick: (Kost) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onSeeAllClick) {
                Text("Lihat Semua")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading && kosts.isEmpty()) {
                items(3) {
                    ShimmerKostCardPlaceholder(modifier = Modifier.width(220.dp))
                }
            } else {
                items(kosts) { kost ->
                    HorizontalKostCard(kost = kost, onClick = { onKostClick(kost) })
                }
            }
        }
    }
}

@Composable
fun HorizontalKostCard(kost: Kost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            Column {
                AsyncImage(
                    model = kost.imageUrl,
                    contentDescription = kost.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_placeholder),
                    error = painterResource(R.drawable.ic_error)
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = kost.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val averageRating = if (kost.ratings.isNotEmpty()) {
                            kost.ratings.map { it.rating }.average()
                        } else {
                            0.0
                        }
                        Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Text(
                            text = " ${String.format(Locale.US, "%.1f", averageRating)} | ${kost.location}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (kost.promoPrice != null) {
                        Column {
                            Text(
                                text = kost.price,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = kost.promoPrice,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text(
                            text = kost.price,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallPromoCard(kost: Kost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            Column {
                AsyncImage(
                    model = kost.imageUrl,
                    contentDescription = kost.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_placeholder),
                    error = painterResource(R.drawable.ic_error)
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = kost.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (kost.promoPrice != null) {
                        Column {
                            Text(
                                text = kost.price,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = kost.promoPrice,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            text = kost.price,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            if (kost.tags.contains("Promo")) {
                PromoBanner()
            }
        }
    }
}

@Composable
fun BoxScope.PromoBanner() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .background(
                color = Color(0xFFE53935),
                shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Promo",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "PROMO",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun PromoNgebutBackground() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4A148C),
                    Color(0xFF8E24AA),
                    Color(0xFFD500F9)
                ),
                start = Offset(0f, 0f),
                end = Offset(canvasWidth, canvasHeight)
            )
        )

        drawLine(
            color = Color.Yellow.copy(alpha = 0.5f),
            start = Offset(x = -50f, y = canvasHeight * 0.4f),
            end = Offset(x = canvasWidth * 0.7f, y = canvasHeight * 0.2f),
            strokeWidth = 12f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(x = canvasWidth * 0.3f, y = canvasHeight + 50f),
            end = Offset(x = canvasWidth * 1.1f, y = canvasHeight * 0.5f),
            strokeWidth = 8f
        )
        drawLine(
            color = Color(0xFFE040FB).copy(alpha = 0.4f),
            start = Offset(x = canvasWidth + 20f, y = canvasHeight),
            end = Offset(x = -20f, y = canvasHeight * 0.9f),
            strokeWidth = 15f,
            cap = StrokeCap.Round
        )

        drawIntoCanvas {
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = 120f
                color = android.graphics.Color.WHITE
                alpha = 30
                style = android.graphics.Paint.Style.FILL
                textAlign = android.graphics.Paint.Align.LEFT
                isFakeBoldText = true
            }
            it.nativeCanvas.rotate(-15f, canvasWidth / 2, canvasHeight / 2)
            it.nativeCanvas.drawText(
                "PROMO",
                canvasWidth * 0.1f,
                canvasHeight * 0.35f,
                paint
            )
            it.nativeCanvas.drawText(
                "NGEBUT",
                canvasWidth * 0.2f,
                canvasHeight * 0.6f,
                paint
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualCategoryCard(
    category: VisualCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoSlidingCarousel(imageUrls: List<String>) {
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    LaunchedEffect(Unit) {
        while(true) {
            delay(3000)
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
fun CategoryResultScreen(
    navController: NavController,
    category: String,
    kostViewModel: KostViewModel = viewModel()
) {
    val allKosts by kostViewModel.kostList.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()

    val filteredKosts = remember(category, allKosts) {
        allKosts.filter { it.type.equals(category, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kategori: $category") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(5) { ShimmerKostCardPlaceholder() }
            }
        } else if (filteredKosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada kost ditemukan untuk kategori \"$category\".")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(filteredKosts) { kost ->
                    KostCardItem(kost = kost, onClick = {
                        navController.navigate("detail/${kost.id}")
                    })
                }
            }
        }
    }
}