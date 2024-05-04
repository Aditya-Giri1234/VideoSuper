package com.example.videosuper.service

import android.app.Application
import com.example.videosuper.util.AppLifeCycleManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLifeCycleManager.initialize(this)
    }
}