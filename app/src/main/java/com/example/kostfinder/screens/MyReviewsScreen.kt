package com.example.kostfinder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.models.Rating
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    navController: NavController,
    kostViewModel: KostViewModel = viewModel()
) {
    val allKosts by kostViewModel.kostList.collectAsState()
    val currentUserId = Firebase.auth.currentUser?.uid

    val myReviews = allKosts.flatMap { kost ->
        kost.ratings
            .filter { it.userId == currentUserId }
            .map { rating -> Pair(kost.name, rating) }
    }.sortedByDescending { it.second.createdAt }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ulasan Saya") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (myReviews.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Anda belum pernah memberikan ulasan.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myReviews) { (kostName, rating) ->
                    MyReviewItem(kostName = kostName, rating = rating)
                }
            }
        }
    }
}

@Composable
fun MyReviewItem(kostName: String, rating: Rating) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(kostName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107))
                Text(" ${rating.rating}", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("\"${rating.comment}\"")
            Spacer(modifier = Modifier.height(8.dp))
            val date = rating.createdAt?.let {
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(it)
            } ?: ""
            Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
        }
    }
}