package com.example.kostfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.kostfinder.data.ThemeDataStore
import com.example.kostfinder.navigation.AppNavigation
import com.example.kostfinder.ui.theme.KostFinderTheme

class MainActivity : ComponentActivity() {

    private lateinit var themeViewModel: ThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeDataStore = ThemeDataStore(this)
        themeViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ThemeViewModel(themeDataStore) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[ThemeViewModel::class.java]

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = false)
            KostFinderTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}