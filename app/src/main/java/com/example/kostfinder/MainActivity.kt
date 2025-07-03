package com.example.kostfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import com.example.kostfinder.navigation.AppNavigation
import com.example.kostfinder.ui.theme.KostFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KostFinderTheme {
                val navController = rememberNavController()

                var name by rememberSaveable { mutableStateOf("Gusde Artadwana") }
                var email by rememberSaveable { mutableStateOf("gusdeartadwana@gmail.com") }

                AppNavigation(
                    navController = navController,
                    name = name,
                    email = email,
                    onUpdateProfile = { newName, newEmail ->
                        name = newName
                        email = newEmail
                    }
                )
            }
        }
    }
}
