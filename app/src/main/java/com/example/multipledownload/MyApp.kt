package com.example.multipledownload

import android.app.Application
import com.downloader.PRDownloader

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        PRDownloader.initialize(applicationContext)
    }
}