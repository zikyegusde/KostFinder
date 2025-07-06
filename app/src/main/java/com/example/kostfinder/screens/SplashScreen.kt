package com.example.kostfinder.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kostfinder.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // State untuk mengontrol animasi utama
    val logoScale = remember { Animatable(0.5f) }
    val taglineAlpha = remember { Animatable(0f) }

    // State untuk animasi ikon-ikon tambahan
    val iconAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Animasi logo membesar
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )

        // Animasi ikon tambahan dan tagline muncul
        iconAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        // Tahan layar sejenak
        delay(1500L)

        // Pindah ke halaman login
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // Tampilan UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Box untuk menampung semua elemen, termasuk ikon-ikon di posisi absolut
        Box(
            modifier = Modifier
                .size(300.dp), // Area untuk animasi ikon
            contentAlignment = Alignment.Center
        ) {
            // Ikon-ikon dekoratif di sekitar logo
            DecorativeIcon(
                icon = Icons.Default.Key,
                modifier = Modifier
                    .align(Alignment.TopStart) // Pojok kiri atas
                    .padding(16.dp)
                    .alpha(iconAlpha.value)
            )
            DecorativeIcon(
                icon = Icons.Default.Bed,
                modifier = Modifier
                    .align(Alignment.TopEnd) // Pojok kanan atas
                    .padding(16.dp)
                    .alpha(iconAlpha.value)
            )
            DecorativeIcon(
                icon = Icons.Default.LocationOn,
                modifier = Modifier
                    .align(Alignment.BottomStart) // Pojok kiri bawah
                    .padding(16.dp)
                    .alpha(iconAlpha.value)
            )

            // Kolom untuk Logo dan Tagline di tengah
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo yang dianimasikan
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo KostFinder",
                    modifier = Modifier
                        .size(150.dp)
                        .scale(logoScale.value)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline yang dianimasikan
                Text(
                    text = "#EnaknyaNgekos",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.alpha(taglineAlpha.value)
                )
            }
        }
    }
}

// Composable terpisah untuk ikon dekoratif agar kode lebih rapi
@Composable
fun DecorativeIcon(icon: ImageVector, modifier: Modifier = Modifier) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.size(32.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) // Warna ikon dibuat sedikit transparan
    )
}