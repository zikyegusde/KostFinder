package com.example.kostfinder.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kostfinder.AuthViewModel
import com.example.kostfinder.KostViewModel
import com.example.kostfinder.UserViewModel
import com.example.kostfinder.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
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
            HomeScreen(navController, kostViewModel)
        }
        composable("admin") {
            AdminScreen(navController, kostViewModel)
        }
        composable("detail/{kostId}") { backStackEntry ->
            val kostId = backStackEntry.arguments?.getString("kostId")
            if (kostId != null) {
                DetailScreen(kostId, navController, kostViewModel, userViewModel)
            }
        }
    }
}