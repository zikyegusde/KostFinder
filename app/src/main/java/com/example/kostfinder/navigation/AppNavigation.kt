package com.example.kostfinder.navigation

import androidx.compose.runtime.Composable
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
        composable("about") {
            AboutScreen(navController)
        }
        composable("terms") {
            TermsScreen(navController)
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(navController)
        }
        composable("editProfile") {
            val user = Firebase.auth.currentUser
            val context = LocalContext.current

            if (user == null) {
                navController.navigate("login") { popUpTo(0) }
            } else {
                EditProfileScreen(
                    currentName = user.displayName ?: "",
                    currentEmail = user.email ?: "",
                    onSaveClick = { newName, newEmail ->
                        userViewModel.updateUserProfile(newName, newEmail, context) { success ->
                            if (success) {
                                navController.popBackStack()
                            }
                        }
                    },
                    onCancelClick = {
                        navController.popBackStack()
                    }
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