package com.example.kostfinder.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kostfinder.R
import com.example.kostfinder.ThemeViewModel
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.data.ThemeDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    userViewModel: UserViewModel = viewModel()
) {
    val userData by userViewModel.userData.collectAsState()
    val context = LocalContext.current
    // Inisialisasi ViewModel untuk tema
    val themeDataStore = remember { ThemeDataStore(context) }
    val themeViewModel: ThemeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ThemeViewModel(themeDataStore) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Menggunakan warna dari tema
                .verticalScroll(rememberScrollState())
        ) {
            // User Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = userData?.profileImageUrl ?: R.drawable.ic_placeholder,
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_placeholder),
                    error = painterResource(id = R.drawable.ic_placeholder)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    userData?.name ?: "Pengguna",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground // Menggunakan warna dari tema
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    userData?.email ?: "Tidak ada email",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            // Menu Section
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem(
                    text = "Edit Profil",
                    icon = Icons.Default.Edit,
                    onClick = onEditProfileClick
                )
                ProfileMenuItem(
                    text = "Riwayat Booking",
                    icon = Icons.Default.History,
                    onClick = { navController.navigate("booking_history") }
                )
                ProfileMenuItem(
                    text = "Ulasan Saya",
                    icon = Icons.Default.Star,
                    onClick = { navController.navigate("my_reviews") }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                // --- Fitur Mode Gelap Ditambahkan Di Sini ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = "Dark Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Mode Gelap",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                themeViewModel.setTheme(isChecked)
                            }
                        }
                    )
                }
                // -----------------------------------------

                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ProfileMenuItem(
                    text = "Tentang Aplikasi",
                    icon = Icons.Default.Info,
                    onClick = { navController.navigate("about") }
                )
                ProfileMenuItem(
                    text = "Syarat dan Ketentuan",
                    icon = Icons.Default.Description,
                    onClick = { navController.navigate("terms") }
                )
                ProfileMenuItem(
                    text = "Kebijakan Privasi",
                    icon = Icons.Default.Shield,
                    onClick = { navController.navigate("privacy_policy") }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                ProfileMenuItem(
                    text = "Logout",
                    icon = Icons.Default.Logout,
                    onClick = onLogoutClick,
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            // Menggunakan warna dari tema agar teks bisa beradaptasi
            color = if (tint != MaterialTheme.colorScheme.primary) tint else MaterialTheme.colorScheme.onBackground
        )
        if (text != "Logout") {
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Go to $text",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}