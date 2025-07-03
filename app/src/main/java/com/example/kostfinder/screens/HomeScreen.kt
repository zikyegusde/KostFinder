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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.kostfinder.data.kostList
import com.example.kostfinder.models.Kost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedItem by remember { mutableStateOf("home") }
    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KostFinder - Jimbaran") },
                actions = {
                    TextButton(onClick = {
                        navController.navigate("admin")
                    }) {
                        Text("Admin", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedItem = selectedItem) {
                selectedItem = it
                bottomNavController.navigate(it) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
        NavHost(
            navController = bottomNavController,
            startDestination = "home"
        ) {
            composable("home") {
                LazyColumn {
                    items(kostList) { kost ->
                        KostListItem(kost = kost, onClick = {
                            navController.navigate("detail/${kost.id}")
                        })
                    }
                }
            }
            composable("search") {
                SearchScreen(navController)
            }
            composable("favorites") {
                FavoritesScreen(
                    onKostClick = { kost ->
                        navController.navigate("detail/${kost.id}")
                    }
                )
            }
            composable("profile") {
                ProfileScreen(
                    name = "Gusde Artadwana",
                    email = "gusdeartadwana@gmail.com",
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onEditProfileClick = {
                        navController.navigate("editProfile")
                    }
                )
                }
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
                painter = rememberAsyncImagePainter(kost.imageUrl),
                contentDescription = kost.name,
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
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
            selected = selectedItem == "home",
            onClick = { onItemSelected("home") },
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
