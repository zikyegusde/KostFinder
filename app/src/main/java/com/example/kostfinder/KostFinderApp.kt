package com.example.kostfinder

import android.app.Application
import com.cloudinary.android.MediaManager

class KostFinderApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Konfigurasi untuk inisialisasi Cloudinary
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "dmjr8ja9n"
        config["api_key"] = "728791363828356"
        config["api_secret"] = "J51SufSqXtpKOIJmDWmfqGpm_5c"

        // Inisialisasi MediaManager
        MediaManager.init(this, config)
    }
}