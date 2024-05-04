package com.example.videosuper.util


import android.app.Activity
import android.app.Application
import android.os.Bundle

object AppLifeCycleManager : Application.ActivityLifecycleCallbacks {
    var appInForeground = false
        private set

    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        appInForeground = true
        // Handle foreground behavior
    }

    override fun onActivityStopped(activity: Activity) {
        appInForeground = false
        // Handle background behavior
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}






