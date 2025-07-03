@file:Suppress("DEPRECATION")

package com.example.kostfinder.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kostfinder.AuthViewModel
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.screens.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberAnimatedNavController()
    val kostViewModel: KostViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    AnimatedNavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
    ) {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        // Rute baru untuk registrasi
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("home") {
            HomeScreen(navController, kostViewModel)
        }
        composable("admin") {
            AdminScreen(navController, kostViewModel)
        }
        composable("detail/{kostId}") { backStackEntry ->
            val kostId = backStackEntry.arguments?.getString("kostId")
            if (kostId != null) {
                DetailScreen(kostId, navController, kostViewModel)
            }
        }
        composable("search") {
            SearchScreen(navController, kostViewModel)
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                onLogoutClick = { /* Logic is now inside ProfileScreen */ },
                onEditProfileClick = { navController.navigate("editProfile") }
            )
        }
        composable("editProfile") {
            EditProfileScreen(
                currentName = "",
                currentEmail = "",
                onSaveClick = { _, _ -> navController.popBackStack() },
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable("favorites") {
            FavoritesScreen(onKostClick = { kost ->
                navController.navigate("detail/${kost.id}")
            })
        }
    }
}
