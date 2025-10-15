package com.example.kostfinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostfinder.data.ThemeDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeDataStore: ThemeDataStore) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = themeDataStore.isDarkTheme
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeDataStore.setTheme(isDark)
        }
    }
}