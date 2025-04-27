package com.tvbc.tvbcapps.storage

import android.content.Context
import com.cloudinary.android.MediaManager
import java.util.HashMap

object CloudinaryConfig {
    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dpuasrpfm" // Sesuai dari dashboard Cloudinary Anda
            config["api_key"] = "167323579311748" // Ganti dengan API key Anda
            config["api_secret"] = "ZUQ4dLHqaL336uhe_J0KQtl0mOw" // Ganti dengan API secret Anda

            MediaManager.init(context, config)
            isInitialized = true
        }
    }
}