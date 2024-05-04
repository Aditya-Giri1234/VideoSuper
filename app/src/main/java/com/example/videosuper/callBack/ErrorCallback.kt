package com.example.videosuper.callBack

interface ErrorCallback {
    fun onError()
    fun onBusyError()
    fun onUnAvailable()
    fun onReject()
}