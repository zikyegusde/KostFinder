package com.example.kostfinder.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kostfinder.AuthViewModel
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // ViewModels yang akan dibagikan
    val kostViewModel: KostViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("home") {
            HomeScreen(navController, kostViewModel, userViewModel)
        }
        composable("admin") {
            AdminScreen(navController, kostViewModel)
        }
        composable("detail/{kostId}") { backStackEntry ->
            val kostId = backStackEntry.arguments?.getString("kostId")
            if (kostId != null) {
                DetailScreen(kostId, navController, kostViewModel, userViewModel, authViewModel)
            }
        }
        composable("about") {
            AboutScreen(navController)
        }
        composable("terms") {
            TermsScreen(navController)
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(navController)
        }
        composable("booking_history") {
            BookingHistoryScreen(navController, userViewModel)
        }
        composable("my_reviews") {
            MyReviewsScreen(navController, kostViewModel)
        }

        composable(
            route = "full_kost_list/{listType}",
            arguments = listOf(navArgument("listType") { type = NavType.StringType })
        ) { backStackEntry ->
            val listType = backStackEntry.arguments?.getString("listType")
            FullKostListScreen(navController = navController, listType = listType, kostViewModel = kostViewModel)
        }

        composable(
            route = "search?category={category}",
            arguments = listOf(navArgument("category") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            SearchScreen(
                navController = navController,
                kostViewModel = kostViewModel,
                initialCategory = category
            )
        }

        // ## PENAMBAHAN: Route baru untuk halaman hasil kategori ##
        composable(
            route = "category_result/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            if (category != null) {
                CategoryResultScreen(
                    navController = navController,
                    category = category,
                    kostViewModel = kostViewModel
                )
            }
        }

        composable("editProfile") {
            val userData by userViewModel.userData.collectAsState()
            val context = LocalContext.current

            if (userData == null) {
                navController.navigate("login") { popUpTo(0) }
            } else {
                EditProfileScreen(
                    currentName = userData?.name ?: "",
                    currentEmail = userData?.email ?: "",
                    currentImageUrl = userData?.profileImageUrl,
                    onSaveClick = { newName, newEmail ->
                        userViewModel.updateUserProfile(newName, newEmail, context) { success ->
                            if (success) {
                                navController.popBackStack()
                            }
                        }
                    },
                    onCancelClick = {
                        navController.popBackStack()
                    },
                    userViewModel = userViewModel
                )
            }
        }
        composable(
            route = "editKost/{kostId}",
            arguments = listOf(navArgument("kostId") { type = NavType.StringType })
        ) { backStackEntry ->
            val kostId = backStackEntry.arguments?.getString("kostId")
            if (kostId != null) {
                EditKostScreen(navController = navController, kostId = kostId, kostViewModel = kostViewModel)
            } else {
                navController.popBackStack()
            }
        }
    }
}