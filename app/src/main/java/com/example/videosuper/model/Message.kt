package com.example.videosuper.model

import com.example.videosuper.util.Constant

data class Message(var sender:String, var target:String, var senderId:String, var roomId:String?=null, var callStatus: Constant.CallStatus, var rejectReason: Constant.UserStatus?=null)
