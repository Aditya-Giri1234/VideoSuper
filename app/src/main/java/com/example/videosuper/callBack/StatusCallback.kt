package com.example.videosuper.callBack

interface StatusCallback {
    fun onFree()
    fun onBusy()
    fun onNotAvailable()
}