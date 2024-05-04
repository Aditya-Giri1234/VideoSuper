package com.example.videosuper.callBack

import com.example.videosuper.model.Message

interface SuccessCallback {
    fun onSuccess(message: Message?=null)
}