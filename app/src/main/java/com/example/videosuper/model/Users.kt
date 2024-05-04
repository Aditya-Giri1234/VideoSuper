package com.example.videosuper.model

import com.example.videosuper.util.Constant

data class Users(var username:String?=null, var status:String=Constant.UserStatus.Free.name, var message:String?=null)
