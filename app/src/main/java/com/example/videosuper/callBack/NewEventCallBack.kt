package com.example.videosuper.callBack

import com.example.videosuper.model.DataModel

interface NewEventCallBack {

    fun onNewEventReceived(dataModel:DataModel,roomId:String)
}