package com.example.kostfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kostfinder.navigation.AppNavigation
import com.example.kostfinder.ui.theme.KostFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KostFinderTheme {
                AppNavigation()
            }
        }
    }
}
