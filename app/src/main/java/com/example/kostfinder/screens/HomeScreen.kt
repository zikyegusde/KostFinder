package com.example.kostfinder.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.kostfinder.models.User
import com.example.kostfinder.screens.common.KostCardItem
import com.example.kostfinder.screens.common.ShimmerKostCardPlaceholder
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

// BottomNavItem dan HomeScreen tidak berubah
data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, kostViewModel: KostViewModel = viewModel()) {
    val bottomNavController = rememberNavController()

    Scaffold(
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
                    kostViewModel = kostViewModel,
                    userViewModel = viewModel()
                )
            }
            composable("search") { SearchScreen(navController, kostViewModel) }
            composable("favorites") { FavoritesScreen(onKostClick = { kost -> navController.navigate("detail/${kost.id}") }) }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    onLogoutClick = {},
                    onEditProfileClick = { navController.navigate("editProfile") }
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    mainNavController: NavController,
    kostViewModel: KostViewModel,
    userViewModel: UserViewModel
) {
    val allKosts by kostViewModel.kostList.collectAsState()
    val promoKosts by kostViewModel.promoKosts.collectAsState()
    val popularKosts by kostViewModel.popularKosts.collectAsState()
    val newKosts by kostViewModel.newKosts.collectAsState()
    val isLoading by kostViewModel.isLoading.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedKabupaten by remember { mutableStateOf<String?>(null) }


    val categories = listOf("Semua", "Kabupaten", "Putra", "Putri", "Campur")
    val kabupatenOptions = listOf("Badung", "Bangli", "Buleleng", "Denpasar", "Gianyar", "Jembrana", "Karangasem", "Klungkung", "Tabanan")


    val filteredList = remember(selectedCategory, selectedKabupaten, allKosts) {
        when {
            selectedKabupaten != null -> {
                allKosts.filter { it.location.equals(selectedKabupaten, ignoreCase = true) }
            }
            selectedCategory == "Semua" -> allKosts
            selectedCategory in listOf("Putra", "Putri", "Campur") -> {
                allKosts.filter { it.type.equals(selectedCategory, ignoreCase = true) }
            }
            else -> allKosts
        }
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { GreetingHeader(user = userData) }

        item {
            val imageUrls = listOf(
                "https://tse3.mm.bing.net/th/id/OIP.L4QxNrmQhPGgWnsTJdbCoQAAAA?pid=Api&P=0&h=180",
                "https://tse3.mm.bing.net/th/id/OIP.LCrvvcBSz2cfxkN4O31x8gHaDt?pid=Api&P=0&h=180",
                "https://tse4.mm.bing.net/th/id/OIP.DZJjoUJwuOmKAB0zG4LqgwHaEK?pid=Api&P=0&h=180",
                "https://tse2.mm.bing.net/th/id/OIP.qSKThw4ORzM76-Rrcd_TnwHaE8?pid=Api&P=0&h=180"
            )
            AutoSlidingCarousel(imageUrls = imageUrls)
        }


        item {
            CategoryChips(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    if (it != "Kabupaten") {
                        selectedKabupaten = null
                    }
                },
                kabupatenOptions = kabupatenOptions,
                selectedKabupaten = selectedKabupaten,
                onKabupatenSelected = {
                    selectedKabupaten = it
                    selectedCategory = "Kabupaten"
                }
            )
        }


        if (selectedCategory == "Semua" && selectedKabupaten == null) {
            item {
                RecommendationSession(
                    title = "Promo Spesial",
                    kosts = promoKosts,
                    isLoading = isLoading,
                    onKostClick = { mainNavController.navigate("detail/${it.id}") }
                )
            }
            item {
                RecommendationSession(
                    title = "Kost Populer",
                    kosts = popularKosts,
                    isLoading = isLoading,
                    onKostClick = { mainNavController.navigate("detail/${it.id}") }
                )
            }
            item {
                RecommendationSession(
                    title = "Baru Ditambahkan",
                    kosts = newKosts,
                    isLoading = isLoading,
                    onKostClick = { mainNavController.navigate("detail/${it.id}") }
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
                        Text("Tidak ada kost ditemukan untuk kategori ini.")
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

// GreetingHeader, AutoSlidingCarousel, CategoryChips, dan RecommendationSession tidak berubah
@Composable
fun GreetingHeader(user: User?) {
    val calendar = Calendar.getInstance()
    val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Selamat Pagi,"
        in 12..14 -> "Selamat Siang,"
        in 15..17 -> "Selamat Sore,"
        else -> "Selamat Malam,"
    }
    val userName = user?.name?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Pengguna"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Text(
            text = greeting,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )
        Text(
            text = userName,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
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
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    kabupatenOptions: List<String>,
    selectedKabupaten: String?,
    onKabupatenSelected: (String) -> Unit
) {
    var kabupatenExpanded by remember { mutableStateOf(false) }


    Column {
        Text(
            text = "Kategori Pilihan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                if (category == "Kabupaten") {
                    Box {
                        FilterChip(
                            selected = selectedCategory == "Kabupaten",
                            onClick = {
                                onCategorySelected("Kabupaten")
                                kabupatenExpanded = true
                            },
                            label = { Text(selectedKabupaten ?: "Kabupaten") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            leadingIcon = if (selectedCategory == "Kabupaten") {
                                { Icon(imageVector = Icons.Default.Done, contentDescription = "Done") }
                            } else {
                                null
                            }
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
                } else {
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
    }
}

@Composable
fun RecommendationSession(
    title: String,
    kosts: List<Kost>,
    isLoading: Boolean,
    onKostClick: (Kost) -> Unit
) {
    if (isLoading || kosts.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    items(3) {
                        ShimmerKostCardPlaceholder()
                    }
                } else {
                    items(kosts) { kost ->
                        HorizontalKostCard(kost = kost, onClick = { onKostClick(kost) })
                    }
                }
            }
        }
    }
}

// ## PERUBAHAN PADA HorizontalKostCard ##
@Composable
fun HorizontalKostCard(kost: Kost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable{ onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
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

                // ## PERUBAHAN UNTUK HARGA PROMO ##
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
                // ## AKHIR PERUBAHAN ##
            }
        }
    }
}