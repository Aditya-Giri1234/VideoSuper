package com.example.videosuper.util

import android.content.Context
import android.util.Log
import android.widget.Toast

object Helper {
    private var toast: Toast?=null

    fun customLog(tag:String , msg:String){
        Log.e(tag ,msg)
    }

    fun customToast(msg: String, context: Context, duration: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, msg, duration).apply { show() }
    }
}