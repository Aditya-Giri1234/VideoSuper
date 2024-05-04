package com.example.videosuper.model

import android.content.Context
import android.content.SharedPreferences

class Preference(var context :Context) {

     var pref:SharedPreferences = context.getSharedPreferences("Users", Context.MODE_PRIVATE)
    var edit:SharedPreferences.Editor = pref.edit()

    fun setUsername(username:String){
        edit.putString("username",username)
        edit.apply()
    }
    fun setTime(usernameId:String?=null){
        if(usernameId==null){
            edit.putString("time",System.currentTimeMillis().toString())
            edit.apply()
        }
        else{
            edit.putString("time",usernameId)
            edit.apply()
        }

    }
    fun getUsername():String?{
        return pref.getString("username","")
    }
    fun getTime():String?{
        return pref.getString("time","")
    }
    fun getRoomId():String{
        return "${System.currentTimeMillis()}${getUsername()}"
    }
}