package com.example.kostfinder.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

        // --- TAMBAHKAN RUTE BARU UNTUK EDIT PROFIL DI SINI ---
        composable("editProfile") {
            val user = Firebase.auth.currentUser
            val context = LocalContext.current

            // Jika pengguna tidak login, kembalikan ke halaman login
            if (user == null) {
                navController.navigate("login") { popUpTo(0) }
            } else {
                EditProfileScreen(
                    currentName = user.displayName ?: "",
                    currentEmail = user.email ?: "",
                    onSaveClick = { newName, newEmail ->
                        // Panggil fungsi dari ViewModel untuk update
                        userViewModel.updateUserProfile(newName, newEmail, context) { success ->
                            if (success) {
                                // Jika berhasil, kembali ke halaman sebelumnya
                                navController.popBackStack()
                            }
                        }
                    },
                    onCancelClick = {
                        // Kembali ke halaman sebelumnya
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}