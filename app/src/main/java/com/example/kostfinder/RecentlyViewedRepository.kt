package com.example.kostfinder

import android.content.Context
import android.content.SharedPreferences

class RecentlyViewedRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("kostfinder_prefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    private val keyRecentlyViewed = "recently_viewed_ids"
    private val separator = ","

    // Fungsi untuk menyimpan daftar ID ke SharedPreferences
    fun saveRecentlyViewed(ids: List<String>) {
        val asString = ids.joinToString(separator)
        editor.putString(keyRecentlyViewed, asString)
        editor.apply()
    }

    // Fungsi untuk mengambil daftar ID dari SharedPreferences
    fun getRecentlyViewed(): List<String> {
        val asString = prefs.getString(keyRecentlyViewed, "") ?: ""
        if (asString.isEmpty()) {
            return emptyList()
        }
        return asString.split(separator)
    }
}