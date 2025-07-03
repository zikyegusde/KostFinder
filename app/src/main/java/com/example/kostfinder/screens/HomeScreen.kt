package com.example.kostfinder.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.models.Kost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, kostViewModel: KostViewModel = viewModel()) {
    val bottomNavController = rememberNavController()
    var selectedItem by remember { mutableStateOf("home_content") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KostFinder - Jimbaran") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedItem = selectedItem) { route ->
                selectedItem = route
                bottomNavController.navigate(route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_content",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home_content") {
                val kostList by kostViewModel.kostList.collectAsState()
                val isLoading by kostViewModel.isLoading.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && kostList.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(kostList) { kost ->
                                KostListItem(kost = kost, onClick = {
                                    navController.navigate("detail/${kost.id}")
                                })
                            }
                        }
                    }
                }
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
                    onLogoutClick = { /* Logic is now inside ProfileScreen */ },
                    onEditProfileClick = { navController.navigate("editProfile") }
                )
            }
        }
    }
}

@Composable
fun KostListItem(kost: Kost, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "scale"
    )
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX.value
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    pressed = true
                    coroutineScope.launch {
                        offsetX.animateTo(
                            targetValue = 20f,
                            animationSpec = tween(durationMillis = 100)
                        )
                        offsetX.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 100)
                        )
                        delay(80)
                        onClick()
                        pressed = false
                    }
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.height(120.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = kost.imageUrl),
                contentDescription = kost.name,
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(kost.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(kost.location, style = MaterialTheme.typography.bodyMedium)
                Text(kost.price, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedItem == "home_content",
            onClick = { onItemSelected("home_content") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedItem == "search",
            onClick = { onItemSelected("search") },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = selectedItem == "favorites",
            onClick = { onItemSelected("favorites") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites") }
        )
        NavigationBarItem(
            selected = selectedItem == "profile",
            onClick = { onItemSelected("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
