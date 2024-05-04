package com.example.videosuper.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TransportInfo
import android.os.PowerManager
import java.security.Provider.Service

object SoftwareManager {


    fun isInternetAvailable(context: Context) = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
        ?.run { getNetworkCapabilities(activeNetwork)?.run { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } }
        ?: false



    fun isMyAppInForeground():Boolean{
        return AppLifeCycleManager.appInForeground
    }

    private fun isMyScreenOff(context: Context) = !(context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive

    private fun wakeUpMyScreen(context: Context) {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                acquire(10 * 1000L /*10 Second*/)
            }
        }
    }

    fun screenCheck(context: Context) = if (isMyScreenOff(context)) wakeUpMyScreen(context) else Unit



}