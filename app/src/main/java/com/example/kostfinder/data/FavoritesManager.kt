package com.example.kostfinder.data

import com.example.kostfinder.models.Kost

object FavoritesManager {
    private val favorites = mutableListOf<Kost>()

    fun addFavorite(kost: Kost) {
        if (!favorites.contains(kost)) {
            favorites.add(kost)
        }
    }

    fun removeFavorite(kost: Kost) {
        favorites.remove(kost)
    }

    fun isFavorite(kost: Kost): Boolean {
        return favorites.contains(kost)
    }

    fun getFavorites(): List<Kost> = favorites
}
