@file:Suppress("DEPRECATION")

package com.example.kostfinder.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.kostfinder.data.kostList
import com.example.kostfinder.screens.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    name: String,
    email: String,
    onUpdateProfile: (String, String) -> Unit
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300))
        }
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("search") {
            SearchScreen(navController)
        }

        composable("favorites") {
            FavoritesScreen { kost ->
                navController.navigate("detail/${kost.id}")
            }
        }

        composable("profile") {
            println("Navigated to profile screen with name=$name and email=$email") // debug
            ProfileScreen(
                name = name,
                email = email,
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

        composable("editProfile") {
            EditProfileScreen(
                currentName = name,
                currentEmail = email,
                onSaveClick = { newName, newEmail ->
                    println("Saving new profile: $newName, $newEmail") // debug
                    onUpdateProfile(newName, newEmail)
                    navController.popBackStack()
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("admin") {
            AdminScreen(navController)
        }

        composable("detail/{kostId}") { backStackEntry ->
            val kostId = backStackEntry.arguments?.getString("kostId")?.toIntOrNull()
            val kost = kostList.find { it.id == kostId }
            if (kost != null) {
                DetailScreen(kost, navController)
            }
        }
    }
}
